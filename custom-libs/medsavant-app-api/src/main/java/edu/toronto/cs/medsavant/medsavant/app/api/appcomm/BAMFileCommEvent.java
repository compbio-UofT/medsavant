/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.toronto.cs.medsavant.medsavant.app.api.appcomm;

import java.net.URL;
import org.ut.biolab.medsavant.shared.appapi.MedSavantApp;

/**
 *
 * @author mfiume
 */
public class BAMFileCommEvent extends AppCommEvent<URL> {

    public BAMFileCommEvent(MedSavantApp sender, URL eventData) {
        super(sender, eventData);
    }
    
}
