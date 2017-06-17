package co.aeva.maintenancemode;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MaintenanceModeEvent extends Event implements Cancellable {
	
	public static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	
	@Override
	public HandlerList getHandlers() {
		 return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}
}