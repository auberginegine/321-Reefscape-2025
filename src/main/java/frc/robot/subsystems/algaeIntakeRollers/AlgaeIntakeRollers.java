/* (C) Robolancers 2025 */
package frc.robot.subsystems.algaeIntakeRollers;

import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

// the same mechanism as algaeIntakeClimb but this controls the rollers instead of the pivot
public class AlgaeIntakeRollers extends SubsystemBase {

  private AlgaeIntakeRollersIO io;
  private AlgaeIntakeRollersInputs inputs;

  private PIDController rollerController;
  private SimpleMotorFeedforward feedForward;

  public AlgaeIntakeRollers(AlgaeIntakeRollersIO io, AlgaeIntakeRollersConfig config) {
    this.io = io;
    this.inputs = new AlgaeIntakeRollersInputs();

    rollerController = new PIDController(config.kP(), config.kI(), config.kD());
    feedForward = new SimpleMotorFeedforward(0,config.kV());
  }

  public static AlgaeIntakeRollers create() {
    return RobotBase.isReal()
        ? new AlgaeIntakeRollers(
            new AlgaeIntakeRollersIOSpark(), AlgaeIntakeRollersIOSpark.config) // creates real mechanism if robot, sim if no robot,
        // ideal if disabled robot
        : new AlgaeIntakeRollers(new AlgaeIntakeRollersIOSim(), AlgaeIntakeRollersIOSim.config);
  }

  public static AlgaeIntakeRollers disable() {
    return new AlgaeIntakeRollers(new AlgaeIntakeRollersIOIdeal(), AlgaeIntakeRollersIOIdeal.config);
  }

  public void spinRollers(Voltage volts) {
    io.setRollerVoltage(volts);
  }

  public void goToAngularVelocity(AngularVelocity desiredAngularVelocity) {
    io.setRollerVoltage(
        Volts.of(
            rollerController.calculate(inputs.rollerVelocity.in(RPM), desiredAngularVelocity.in(RPM)) 
            + feedForward.calculate(desiredAngularVelocity.in(RPM))));
  }

  public Command
      intake() { // intakes algae until beam break breaks and registers algae in the mechanism
    return run(() -> spinRollers(AlgaeIntakeRollersConstants.kRollerIntakeVoltage));
  }

  public Command outtake() { // outtakes by spinning rollers outward
    return run(() -> spinRollers(AlgaeIntakeRollersConstants.kRollerOuttakeVoltage));
  }

  public Command setMechanismVoltage(Voltage volts) { // sets whole mechanism voltage
    return run(
        () -> spinRollers(volts)
        );
  }

  @Override // updates inputs constatly
  public void periodic() {
    io.updateInputs(inputs);
  }
}
