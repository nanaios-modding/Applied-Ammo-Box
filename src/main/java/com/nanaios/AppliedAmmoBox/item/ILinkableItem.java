package com.nanaios.AppliedAmmoBox.item;

import appeng.api.features.IGridLinkableHandler;

public interface ILinkableItem {
    default IGridLinkableHandler getLinkableHandler() {return null;};
}
