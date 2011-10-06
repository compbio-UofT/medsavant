package org.ut.biolab.medsavant.listener;

public interface ProjectListener {
        public void projectAdded(String projectName);
        public void projectRemoved(String projectName);
        public void projectChanged(String projectName);

        public void projectTableRemoved(int projid, int refid);
        
        public void referenceChanged(String referenceName);
    }
   