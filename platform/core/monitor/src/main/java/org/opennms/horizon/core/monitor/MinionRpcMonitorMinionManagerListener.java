package org.opennms.horizon.core.monitor;

import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.core.ipc.grpc.server.manager.MinionManagerListener;

public class MinionRpcMonitorMinionManagerListener implements MinionManagerListener {

    private MinionRpcMonitorManager minionRpcMonitorManager;

    public MinionRpcMonitorManager getMinionRpcMonitorManager() {
        return minionRpcMonitorManager;
    }

    public void setMinionRpcMonitorManager(MinionRpcMonitorManager minionRpcMonitorManager) {
        this.minionRpcMonitorManager = minionRpcMonitorManager;
    }

    @Override
    public void onMinionAdded(long sequence, MinionInfo minionInfo) {
        minionRpcMonitorManager.startMonitorMinion(minionInfo);
    }

    @Override
    public void onMinionRemoved(long sequence, MinionInfo minionInfo) {
        minionRpcMonitorManager.stopMonitorMinion(minionInfo.getId());
    }
}
