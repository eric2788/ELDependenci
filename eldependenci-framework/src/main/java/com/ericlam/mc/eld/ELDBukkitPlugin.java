package com.ericlam.mc.eld;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * 用於主類繼承
 */
public abstract class ELDBukkitPlugin extends JavaPlugin {

    @Override
    public final void onLoad() {
        final var provider = ELDependenci.getApi().register(this, this::bindServices);
        this.manageProvider(provider);
    }

    /**
     * 綁定服務，單例與文件的地方
     * @param collection 註冊器
     */
    protected abstract void bindServices(ServiceCollection collection);

    /**
     * 用於生命週期之前的操作
     * @param provider 管理器提供
     */
    protected abstract void manageProvider(ManagerProvider provider);

    @Override
    public final void onEnable() {
        getLogger().info("Enabling plugin "+this.getName());
        getLogger().info("This plugin is hooking the lifecycle of ELDependenci framework.");
    }

    @Override
    public final void onDisable() {
        getLogger().info("Disabling plugin "+this.getName());
    }
}
