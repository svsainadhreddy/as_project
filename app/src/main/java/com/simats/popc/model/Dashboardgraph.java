package com.simats.popc.model;

public class Dashboardgraph {
        private double stable;
        private double pending;
        private double high_risk;
        private int total_patients;

        public double getStable() {
                return stable;
        }

        public void setStable(double stable) {
                this.stable = stable;
        }

        public double getPending() {
                return pending;
        }

        public void setPending(double pending) {
                this.pending = pending;
        }

        public double getHigh_risk() {
                return high_risk;
        }

        public void setHigh_risk(double high_risk) {
                this.high_risk = high_risk;
        }

        public int getTotal_patients() {
                return total_patients;
        }

        public void setTotal_patients(int total_patients) {
                this.total_patients = total_patients;
        }
}
