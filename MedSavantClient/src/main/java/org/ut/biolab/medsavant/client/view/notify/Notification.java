/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.view.notify;

import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import org.ut.biolab.medsavant.client.api.Listener;

/**
 * A notification to be shown in the notification panel
 */
public class Notification {

    private ImageIcon icon;
    private String name = "";
    private String description = "";
    private double progress = -1;
    private boolean showsProgress = false;
    private boolean isIndeterminateProgress = false;
    private boolean timesout = false;
    private boolean canHide = true;
    private String actionName = "";
    private ActionListener action;
    private Listener<Notification> listener;
    private boolean isHidden = false;
    private boolean isClosed;
    private boolean hideDoesClose = true;

    public Notification() {
    }

    public String getName() {
        return name;
    }

    public double getProgress() {
        return progress;
    }

    public boolean isShowsProgress() {
        return showsProgress;
    }

    public boolean isIndeterminateProgress() {
        return isIndeterminateProgress;
    }

    public boolean isTimesout() {
        return timesout;
    }

    public boolean canHide() {
        return canHide;
    }

    public String getActionName() {
        return actionName;
    }

    public ActionListener getAction() {
        return action;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setDescription(String description) {
        this.description = description;
        updateListener();
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public void setName(String name) {
        this.name = name;
        updateListener();
    }

    public void setProgress(double progress) {
        this.progress = progress;
        updateListener();
    }

    public void setShowsProgress(boolean showsProgress) {
        this.showsProgress = showsProgress;
        updateListener();
    }

    public void setIsIndeterminateProgress(boolean isIndeterminateProgress) {
        this.isIndeterminateProgress = isIndeterminateProgress;
        updateListener();
    }

    public void setTimesout(boolean timesout) {
        this.timesout = timesout;
        updateListener();
    }

    public void setCanHide(boolean canHide) {
        this.canHide = canHide;
        updateListener();
    }

    public void setAction(String actionName, ActionListener action) {
        this.action = action;
        this.actionName = actionName;
        updateListener();
    }

    protected void addListener(Listener<Notification> listener) {
        this.listener = listener;
    }

    private void updateListener() {
        if (listener != null) {
            listener.handleEvent(this);
        }
    }

    public void hide() {
        isHidden = true;
        if (hideDoesClose) {
            close();
        } else {
            updateListener();
        }
    }

    public void close() {
        isClosed = true;
        updateListener();
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setHideDoesClose(boolean b) {
        hideDoesClose = b;
        updateListener();
    }

    public void unhide() {
        isHidden = false;
        updateListener();
    }
}
