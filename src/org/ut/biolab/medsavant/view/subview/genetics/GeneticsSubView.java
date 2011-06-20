/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview.genetics;

import org.ut.biolab.medsavant.view.subview.Page;
import org.ut.biolab.medsavant.view.subview.SubView;

/**
 *
 * @author mfiume
 */
public class GeneticsSubView extends SubView {

    @Override
    public String getName() {
        return "Genetics";
    }

    @Override
    public Page[] getPages() {
        Page[] pages = new Page[2];
        pages[0] = new GeneticsFilterPage();
        pages[1] = new GeneticsListPage();
        return pages;
    }

}
