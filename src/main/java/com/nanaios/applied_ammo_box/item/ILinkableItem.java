package com.nanaios.applied_ammo_box.item;

import appeng.api.features.IGridLinkableHandler;

public interface ILinkableItem {
    default IGridLinkableHandler getLinkableHandler() {return null;};
}
