package org.ut.biolab.medsavant.project;

public interface ProjectListener {
        public void projectAdded(String projectName);
        public void projectRemoved(String projectName);
        public void projectChanged(String projectName);
        public void projectTableRemoved(int projid, int refid);
}
   