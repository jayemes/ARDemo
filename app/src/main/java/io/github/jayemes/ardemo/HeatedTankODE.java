package io.github.jayemes.ardemo;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;

public class HeatedTankODE implements FirstOrderDifferentialEquations {

    private double FIn, TIn, QDot, TOut, h, aTank, aOut;
    private double cp = 4.184; // kJ/kg-K
    private double rho = 1000; // kg/m3
    private double g = 9.81; // m/s2
    private double kValve = 0.5; // Non dimensional

    public HeatedTankODE(double FIn, double TIn, double TOut, double QDot, double h, double aTank, double aOut) {
        this.FIn = FIn;
        this.TIn = TIn;
        this.TOut = TOut;
        this.QDot = QDot;
        this.h = h;
        this.aTank = aTank;
        this.aOut = aOut;
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public void computeDerivatives(double t, double[] vars, double[] derivs) throws MaxCountExceededException, DimensionMismatchException {
        // vars[0] = TOut
        // vars[1] = h
        double mTank = vars[1] * aTank * rho;
        derivs[0] = FIn * rho / mTank * (TIn - vars[0]) + QDot / mTank / cp;
        derivs[1] = FIn / aTank - aOut / aTank * Math.sqrt(vars[1] * g / (0.5 + kValve));

    }

    public void run() {
        FirstOrderIntegrator integrator = new ClassicalRungeKuttaIntegrator(1.0); // Set time step

        FirstOrderDifferentialEquations ODE = this;

        double[] vars0 = new double[]{TOut, h}; // initial state
        double[] vars1 = new double[2]; // array to save the result

        integrator.integrate(ODE, 0.0, vars0, 1.0, vars1); // now TOut1 contains final state at time t=1.0

        TOut = vars1[0];
        h = vars1[1];

    }

    public void setQDot(double QDot) {
        this.QDot = QDot;
    }

    public void setFIn(double FIn) {
        this.FIn = FIn;
    }

    public void setkValve(double kValve) {
        this.kValve = kValve;
    }

    public double getkValve() {
        return kValve;
    }

    public double getFIn() {
        return FIn;
    }

    public double getQDot() {
        return QDot;
    }

    public double getTOut() {
        return TOut;
    }

    public double getH() {
        return h;
    }
}
