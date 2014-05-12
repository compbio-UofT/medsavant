/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package medsavant.uhn.cancer;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSeparator;

public class StatusIconPanel extends JPanel {

    private final ToggleableIcon pendingReviewIcon;
    private final ToggleableIcon approvedIcon;   
    private final ToggleableIcon includedIcon;
    private ToggleableIcon deletedIcon;

    private static final String APPROVED_ICON_ACTIVE_LOCATION = "/approved.png";
    private static final String APPROVED_ICON_INACTIVE_LOCATION = "/gray_approved.png";
    private static final String INCLUDED_ICON_ACTIVE_LOCATION = "/include_in_report.png";
    private static final String INCLUDED_ICON_INACTIVE_LOCATION = "/gray_include_in_report.png";
    private static final String PENDING_REVIEW_ICON_ACTIVE_LOCATION = "/pending.png";
    private static final String PENDING_REVIEW_ICON_INACTIVE_LOCATION = "/gray_pending.png";
    private static final String DELETED_ICON_ACTIVE_LOCATION = "/delete.png";
    private static final String DELETED_ICON_INACTIVE_LOCATION = "/gray_delete.png";

    private static final String PENDING_REVIEW_TOOLTIP = "Pending Review";
    private static final String APPROVED_TOOLTIP = "Approved";
    private static final String INCLUDE_TOOLTIP = "Include in Report";
    private static final String DELETE_TOOLTIP = "Delete";
    public StatusIconPanel(int iconWidth,
            int iconHeight, boolean iconsToggleable,
            boolean isApproved, boolean isIncluded, boolean isPendingReview) {
        super();
        
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.pendingReviewIcon = new ToggleableIcon(PENDING_REVIEW_ICON_ACTIVE_LOCATION, PENDING_REVIEW_ICON_INACTIVE_LOCATION, iconWidth, iconHeight, iconsToggleable, isPendingReview);
        this.approvedIcon = new ToggleableIcon(APPROVED_ICON_ACTIVE_LOCATION, APPROVED_ICON_INACTIVE_LOCATION, iconWidth, iconHeight, iconsToggleable, isApproved);
        this.includedIcon = new ToggleableIcon(INCLUDED_ICON_ACTIVE_LOCATION, INCLUDED_ICON_INACTIVE_LOCATION, iconWidth, iconHeight, iconsToggleable, isIncluded);
        this.add(pendingReviewIcon);       
        //this.add(getSeparator(iconWidth, iconHeight));
        this.add(approvedIcon);
        //this.add(getSeparator(iconWidth, iconHeight));
        this.add(includedIcon);
        this.deletedIcon = null;
        
        this.pendingReviewIcon.setToolTipText(PENDING_REVIEW_TOOLTIP);
        this.approvedIcon.setToolTipText(APPROVED_TOOLTIP);
        this.includedIcon.setToolTipText(INCLUDE_TOOLTIP);
    }

    public StatusIconPanel(int iconWidth,
            int iconHeight, boolean iconsToggleable,
            boolean isApproved, boolean isIncluded, boolean isPendingReview, boolean isDeleted) {
        this(iconWidth, iconHeight, iconsToggleable, isApproved, isIncluded, isPendingReview);
        this.deletedIcon = new ToggleableIcon(DELETED_ICON_ACTIVE_LOCATION, DELETED_ICON_INACTIVE_LOCATION, iconWidth, iconHeight, iconsToggleable, isDeleted);
        this.deletedIcon.setToolTipText(DELETE_TOOLTIP);
        //this.add(getSeparator(iconWidth, iconHeight));
        System.out.println("Adding deleted icon");
        this.add(deletedIcon);
    }

    public ToggleableIcon getPendingReviewIcon() {
        return pendingReviewIcon;
    }

    public ToggleableIcon getApprovedIcon() {
        return approvedIcon;
    }

    public ToggleableIcon getIncludedIcon() {
        return includedIcon;
    }

    public ToggleableIcon getDeletedIcon() {
        return deletedIcon;
    }
    
    
    //Returns a separator set to half the icon width.
    private JSeparator getSeparator(int iconWidth, int iconHeight){
        JSeparator spacer = new JSeparator();
        spacer.setPreferredSize(new Dimension(iconWidth/2, iconHeight));        
        return spacer;
    }    

}
