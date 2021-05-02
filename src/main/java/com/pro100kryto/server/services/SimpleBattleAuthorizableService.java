package com.pro100kryto.server.services;

import com.pro100kryto.server.module.ModuleConnectionSafe;
import com.pro100kryto.server.modules.auth.connection.Connection;
import com.pro100kryto.server.modules.simplebattle.connection.ConnectionRoles;
import com.pro100kryto.server.modules.simplebattle.connection.ISimpleBattleModuleConnection;
import com.pro100kryto.server.modules.simplebattle.connection.PlayerConnectionInfo;
import com.pro100kryto.server.service.AServiceType;
import com.pro100kryto.server.service.Service;
import com.pro100kryto.server.services.simplebattleauthorizable.connection.ISimpleBattleAuthorizableServiceConnection;
import org.jetbrains.annotations.Nullable;

public class SimpleBattleAuthorizableService extends AServiceType<ISimpleBattleAuthorizableServiceConnection> {
    private ModuleConnectionSafe<ISimpleBattleModuleConnection> moduleConnectionSafe;

    public SimpleBattleAuthorizableService(Service service) {
        super(service);
    }

    @Override
    protected void beforeStart() throws Throwable {
        moduleConnectionSafe = new ModuleConnectionSafe<>(service,
                callback.getSettingOrDefault("simplebattle-module-name", "simplebattle"));

    }

    @Nullable
    @Override
    protected ISimpleBattleAuthorizableServiceConnection createServiceConnection() throws Throwable {
        return new SimpleBattleAuthorizableServiceConnection();
    }

    private final class SimpleBattleAuthorizableServiceConnection implements ISimpleBattleAuthorizableServiceConnection{

        @Override
        public void authorize(Connection connection) {
            try {
                moduleConnectionSafe.getModuleConnection().connectPlayer(new PlayerConnectionInfo(
                        connection.getConnId(),
                        connection.getNickname(),
                        new ConnectionRoles(connection.getRoles().getRolesAsInt()),
                        connection.getEndPoint()
                ));
            } catch (Throwable throwable){
                callback.getLogger().writeException(throwable, "Connection "+connection.getConnId()+" refused");
            }
        }

        @Override
        public boolean isAuthorized(int connId) {
            try {
                return moduleConnectionSafe.getModuleConnection().isConnectedPlayer(connId);
            } catch (Throwable ignored){
            }
            return false;
        }

        @Override
        public void reject(int connId) {
            try {
                moduleConnectionSafe.getModuleConnection().disconnectPlayer(connId);
            } catch (Throwable ignored){
            }
        }

        @Override
        public boolean ping() {
            return true;
        }
    }
}
