package com.ericlam.mc.eld;

import java.util.function.Consumer;

public class ELDependenci implements ELDependenciAPI {


    public static ELDependenciAPI getApi() {
        throw new UnsupportedOperationException("not plugin");
    }

    public ManagerProvider register(ELDBukkitPlugin plugin, Consumer<ServiceCollection> injector) {
        return null;
    }


}
