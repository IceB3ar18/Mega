package club.mega.util;

public final class SpringAnimation {

    private double position;
    private double velocity;
    private double target;
    private double stiffness;
    private double damping;

    public SpringAnimation(double initialPosition, double stiffness, double damping) {
        this.position = initialPosition;
        this.stiffness = stiffness;
        this.damping = damping;
        this.velocity = 0;
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public double getPosition() {
        return position;
    }

    public void update(double deltaTime) {
        double displacement = target - position;
        double springForce = stiffness * displacement;
        double dampingForce = -damping * velocity;
        double force = springForce + dampingForce;

        double acceleration = force;
        velocity += acceleration * deltaTime;
        position += velocity * deltaTime;

        // Optional: limit the oscillation to prevent it from going out of control
        if (Math.abs(velocity) < 0.01 && Math.abs(displacement) < 0.01) {
            position = target;
            velocity = 0;
        }
    }
}
