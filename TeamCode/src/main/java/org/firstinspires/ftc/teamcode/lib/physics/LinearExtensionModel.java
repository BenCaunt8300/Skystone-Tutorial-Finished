package org.firstinspires.ftc.teamcode.lib.physics;

public class LinearExtensionModel {
    private final MotorModel motorModel;
    private final double     spoolDiameter; //m

    private final double staticFriction;
    private final double coulombFriction;

    private double position; //m
    private double velocity; //m / s
    private double acceleration; //m / s^2
    private double jerk; //m / s^3

    private double lastAcceleration; //m / s^2

    public LinearExtensionModel(MotorModel motorModel, double spoolDiameter,
                                double staticFriction, double coulombFriction) {
        this.motorModel      = motorModel;
        this.spoolDiameter   = spoolDiameter;
        this.staticFriction  = staticFriction;
        this.coulombFriction = coulombFriction;

        setPosition(0d);
        setVelocity(0d);
        setAcceleration(0d);
        setJerk(0d);

        setLastAcceleration(0d);
    }

    public static void main(String... args) {
        final double backDriveTorque   = 0.01694772439999992d; //N m
        final double mechanismWeight   = 4.448d * 16.5d; //N, 16.5 lbs
        final double gameElementWeight = 0d; //N, 0 lbs
        final double spoolDiameter     = 0.55d * 0.0254d; //m, 0.55 in
        LinearExtensionModel linearExtensionModel = new LinearExtensionModel(
                new MotorModel(
                        1d, 12d, 0.519d * 2d, 9.901d,
                        0.4d, 1479.93621621622d, 1d,
                        (motorPosition) -> backDriveTorque,
                        (motorPosition) -> (mechanismWeight + gameElementWeight) * spoolDiameter / 2d,
                        3E-3, 2E-3, 1E-4, 0.05d, 25d),
                spoolDiameter, 0.0025d, 0.002d
        );

        System.out.println("t\ty\tv\ta");//\tj");
        final double dt = 0.001d;
        for(int i = 0; i < 1000; i++) {
            linearExtensionModel.update(dt, 12d);
            System.out.print((int)(i * dt * 1000d) / 1000d + "\t");
            System.out.println(linearExtensionModel);
        }
    }

    public void update(double dt, double voltageInput) {
        getMotorModel().update(dt, voltageInput, getLinearSlideFrictionTorque());
        setLastAcceleration(getAcceleration());

        setPosition(getMotorModel().getLinearPosition(getSpoolDiameter()));
        setVelocity(getMotorModel().getLinearVelocity(getSpoolDiameter()));
        setAcceleration(getMotorModel().getLinearAcceleration(getSpoolDiameter()));
        setJerk((getAcceleration() - getLastAcceleration()) / dt);
    }

    public double getLinearSlideFrictionTorque() {
        return (getVelocity() == 0 ? getStaticFriction() : getCoulombFriction()) * getSpoolDiameter() / 2d;
    }

    @Override
    public String toString() {
        return getPosition() / 0.0254d + "\t" + getVelocity() / 0.0254d + "\t" + getAcceleration() / 0.0254d; //+ "\t" + getJerk() / 0.0254d;
    }

    public MotorModel getMotorModel() {
        return motorModel;
    }

    public double getSpoolDiameter() {
        return spoolDiameter;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    public double getJerk() {
        return jerk;
    }

    public void setJerk(double jerk) {
        this.jerk = jerk;
    }

    public double getLastAcceleration() {
        return lastAcceleration;
    }

    public void setLastAcceleration(double lastAcceleration) {
        this.lastAcceleration = lastAcceleration;
    }

    public double getStaticFriction() {
        return staticFriction;
    }

    public double getCoulombFriction() {
        return coulombFriction;
    }
}
