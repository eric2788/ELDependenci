package com.ericlam.mc.eld;

import com.ericlam.mc.eld.bukkit.ItemInteractListener;
import com.ericlam.mc.eld.commands.ELDArgumentManager;
import com.ericlam.mc.eld.commands.ELDCommandHandler;
import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.managers.ArgumentManager;
import com.ericlam.mc.eld.managers.ConfigStorage;
import com.ericlam.mc.eld.managers.ItemInteractManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ELDependenci extends JavaPlugin implements ELDependenciAPI, Listener {

    private final ELDModule module = new ELDModule(this);
    private final Map<JavaPlugin, ELDServiceCollection> collectionMap = new ConcurrentHashMap<>();
    private final ELDArgumentManager argumentManager = new ELDArgumentManager();
    private ItemInteractListener itemInteractListener;
    private static ELDependenciAPI api;
    private Injector injector;

    @Override
    public void onLoad() {
        api = this;
        this.itemInteractListener =  new ItemInteractListener(this);
    }

    public static ELDependenciAPI getApi() {
        return api;
    }

    public ManagerProvider register(JavaPlugin plugin, Consumer<ServiceCollection> injector) {
        if (collectionMap.containsKey(plugin)){
            throw new IllegalStateException("本插件已經被註冊，不得重複註冊。");
        }
        var collection = new ELDServiceCollection(module, plugin);
        injector.accept(collection);
        collection.configManager.dumpAll();
        this.collectionMap.put(plugin, collection);
        return new ELDManagerProvider(collection);
    }

    @Override
    public void onEnable() {
        registerParser();
        getServer().getPluginManager().registerEvents(itemInteractListener, this);
        this.injector = Guice.createInjector(module);
        injector.getInstance(InstanceInjector.class).setInjector(injector);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPluginEnable(final PluginEnableEvent e){
        if (!(e.getPlugin() instanceof JavaPlugin)) return;
        var plugin = (JavaPlugin)e.getPlugin();
        var services = collectionMap.get(plugin);
        if (services == null) return; // not eld plugin

        services.configManager.setInjector(injector);
        services.lifeCycleHook.onEnable(plugin);

        //register command
        if (!services.commands.isEmpty()){
            plugin.getLogger().info("正在註冊插件 "+plugin.getName()+" 的所有指令...");
            ELDCommandHandler.registers(plugin, services.commands, injector, argumentManager);
        }

        //register listener
        if (!services.listeners.isEmpty()){
            plugin.getLogger().info("正在註冊插件 "+plugin.getName()+" 的所有監聽器...");
            services.listeners.forEach(listenerCls -> {
                var listener = injector.getInstance(listenerCls);
                plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            });
        }
    }

    @EventHandler
    public void onPluginDisable(final PluginDisableEvent e){
        if (!(e.getPlugin() instanceof JavaPlugin)) return;
        var plugin = (JavaPlugin)e.getPlugin();
        var services = collectionMap.get(plugin);
        if (services == null) return; // not eld plugin

        services.lifeCycleHook.onDisable(plugin);

    }




    private class ELDManagerProvider implements ManagerProvider {

        private final ELDServiceCollection collection;

        private ELDManagerProvider(ELDServiceCollection collection) {
            this.collection = collection;
        }

        @Override
        public ConfigStorage getConfigStorage() {
            return Optional.ofNullable(collection.configManager).orElseThrow(() -> new IllegalStateException("插件未註冊"));
        }

        @Override
        public ArgumentManager getArgumentManager() {
            return argumentManager;
        }

        @Override
        public ItemInteractManager getItemInteractManager() {
            return itemInteractListener;
        }
    }

    private void registerParser() {
        argumentManager.registerParser(Integer.class, (args, sender) -> {
            var num = args.next();
            try {
                return Integer.parseInt(args.next());
            } catch (NumberFormatException e) {
                throw new ArgumentParseException(num + " 不是有效的 Integer 。");
            }
        });
        argumentManager.registerParser(Double.class, (args, sender) -> {
            var num = args.next();
            try {
                return Double.parseDouble(args.next());
            } catch (NumberFormatException e) {
                throw new ArgumentParseException(num + " 不是有效的 Double 。");
            }
        });

        argumentManager.registerParser(Long.class, (args, sender) -> {
            var num = args.next();
            try {
                return Long.parseLong(args.next());
            } catch (NumberFormatException e) {
                throw new ArgumentParseException(num + " 不是有效的 Long 。");
            }
        });

        argumentManager.registerParser(Byte.class, (args, sender) -> {
            var num = args.next();
            try {
                return Byte.parseByte(args.next());
            } catch (NumberFormatException e) {
                throw new ArgumentParseException(num + " 不是有效的 Byte 。");
            }
        });

        argumentManager.registerParser(Short.class, (args, sender) -> {
            var num = args.next();
            try {
                return Short.parseShort(args.next());
            } catch (NumberFormatException e) {
                throw new ArgumentParseException(num + " 不是有效的 Short 。");
            }
        });

        argumentManager.registerParser(Float.class, (args, sender) -> {
            var num = args.next();
            try {
                return Float.parseFloat(args.next());
            } catch (NumberFormatException e) {
                throw new ArgumentParseException(num + " 不是有效的 Float 。");
            }
        });

        argumentManager.registerParser(Character.class, (args, sender) -> args.next().charAt(0));
        argumentManager.registerParser(Boolean.class, (args, sender) -> Boolean.parseBoolean(args.next()));
        argumentManager.registerParser(String.class, (args, sender) -> args.next());
        argumentManager.registerParser(String.class, "message", ((args, sender) -> {
            var builder = new StringBuilder();
            args.forEachRemaining(s -> builder.append(s).append(" "));
            return builder.toString();
        }));
        argumentManager.registerParser(Player.class, (args, sender) -> {
            var player = Bukkit.getPlayer(args.next());
            if (player == null) {
                throw new ArgumentParseException("&c玩家未上線");
            }
            return player;
        });

        argumentManager.registerParser(OfflinePlayer.class, (args, sender) -> {
            var uuid = Bukkit.getPlayerUniqueId(args.next());
            if (uuid == null) {
                throw new ArgumentParseException("&c玩家不存在");
            }
            return Bukkit.getOfflinePlayer(uuid);
        });

    }
}
