/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.command.Command;
import com.ctre.phoenix.motorcontrol.can.*;

//import org.opencv.imgproc.Imgproc;
//import org.usfirst.frc.team3667.robot.Robot.startingPosition;
//import org.usfirst.frc.team3667.robot.Robot.target;

import com.analog.adis16448.frc.ADIS16448_IMU;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends IterativeRobot {
  ADIS16448_IMU imu;
	String gameData = "";
  private UsbCamera camera;
  double lastValidDirection = 0;

  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  private Encoder leftEncoder = new Encoder(0, 1, false, EncodingType.k4X);
  private Encoder rightEncoder = new Encoder(2, 3, true, EncodingType.k4X);
  private DigitalInput limitSwitchHigh = new DigitalInput(6);
  private DigitalInput limitSwitchLow = new DigitalInput(7);
  DoubleSolenoid climbShifter = new DoubleSolenoid(0, 1);
  DoubleSolenoid pneuTilt = new DoubleSolenoid(2, 3);

  WPI_TalonSRX _frontLeftMotor = new WPI_TalonSRX(13);
  WPI_TalonSRX _frontRightMotor = new WPI_TalonSRX(12);
  WPI_TalonSRX _rearRightMotor = new WPI_TalonSRX(11);
  WPI_TalonSRX _rearLeftMotor = new WPI_TalonSRX(10);
  WPI_TalonSRX _lift = new WPI_TalonSRX(14);
  WPI_TalonSRX _lift2 = new WPI_TalonSRX(15);
  WPI_TalonSRX _pickupLeft = new WPI_TalonSRX(16);
  WPI_TalonSRX _pickupRight = new WPI_TalonSRX(17);

  SpeedControllerGroup leftMotors = new SpeedControllerGroup(_frontLeftMotor, _rearLeftMotor);
  SpeedControllerGroup rightMotors = new SpeedControllerGroup(_frontRightMotor, _rearRightMotor);
  DifferentialDrive _drive = new DifferentialDrive(leftMotors, rightMotors);
  Joystick _driveController = new Joystick(0);
  Joystick _cubeController = new Joystick(1);

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for
   * items like diagnostics that you want ran during disabled, autonomous,
   * teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable chooser
   * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
   * remove all of the chooser code and uncomment the getString line to get the
   * auto name from the text box below the Gyro
   *
   * <p>
   * You can add additional auto modes by adding additional comparisons to the
   * switch structure below with additional strings. If using the SendableChooser
   * make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // autoSelected = SmartDashboard.getString("Auto Selector",
    // defaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
    case kCustomAuto:
      // Put custom auto code here
      break;
    case kDefaultAuto:
    default:
      // Put default auto code here
      break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    // Logic to control the Shifting Solenoid to Low Gear
    if (_driveController.getRawButton(1)) {
      climbShifter.set(DoubleSolenoid.Value.kForward);
    }
    // Logic to control the Shifting Solenoid to High Gear
    if (_driveController.getRawButton(2)) {
      climbShifter.set(DoubleSolenoid.Value.kReverse);
    }

    // When the button controlling this is not pressed, the cube-grabbing
    // mechanism is tilted up
    // If it is not, the mechanism will remain at its lowest point.
    if (_driveController.getRawButton(5) || _driveController.getRawButton(6)) {
      pneuTilt.set(DoubleSolenoid.Value.kReverse);
    } else {
      pneuTilt.set(DoubleSolenoid.Value.kForward);
    }

    // Logic for Cube Pickup and Release
    if (_cubeController.getRawButton(5)) {
      _pickupLeft.set(1);
      _pickupRight.set(-1);
    } else if (_cubeController.getRawButton(6)) {
      _pickupLeft.set(-1);
      _pickupRight.set(1);
    } else if (_cubeController.getRawAxis(2) != 0) {
      _pickupLeft.set(_cubeController.getRawAxis(2)); // 7
      _pickupRight.set(_cubeController.getRawAxis(2) * -1.0); // 7
    } else {
      _pickupLeft.set(_cubeController.getRawAxis(3) * -1.0); // 8
      _pickupRight.set(_cubeController.getRawAxis(3)); // 8
    }

    // Logic to Lift and Lower
    double curLiftVal = _cubeController.getRawAxis(1) * -0.8;
    if (curLiftVal > 0.1 && !limitSwitchHigh.get()) {
      _lift.set(curLiftVal);
      _lift2.set(curLiftVal);
    } else if (curLiftVal < -0.1 && !limitSwitchLow.get()) {
      _lift.set(curLiftVal * 0.5);
      _lift2.set(curLiftVal * 0.5);
    } else if (curLiftVal > 0.1 && limitSwitchHigh.get()) {
      _lift.set(0.07);
      _lift2.set(0.07);
    } else {
      _lift.set(0);
      _lift2.set(0);
    }

    // Logic for Cube Tilt
    // _pickupTilt.set(_cubeController.getRawAxis(5)); // was 5

    // Basic logic to drive the robot
    if (!limitSwitchLow.get()) {
      // Slow the robot down when not at low position on lift
      _drive.arcadeDrive(_driveController.getRawAxis(1) * -0.68, _driveController.getRawAxis(4) * 0.68);
    } else {
      // FULL SPEED!!! robot drive (not quite hyper speed though)
      _drive.arcadeDrive(_driveController.getRawAxis(1) * -1, _driveController.getRawAxis(4) * 0.75);
    }

    // Logic to reset sensor for auton testing
    if (_driveController.getRawButton(3)) {
      // When the blue "X" button is pressed on Drive Controller
      initAndResetAll();
    }

  }

  private void initSensorsOnly() {
    imu.reset();
    leftEncoder.reset();
    rightEncoder.reset();
  }

  private void initAndResetAll() {
    initSensorsOnly();
    lastValidDirection = 0;
    // Get FMS Data to determine ownership sides.
    int retries = 50;
    while (gameData.length() < 3 && retries > 0) {
      SmartDashboard.putString("Current Play:", gameData);
      try {
        Thread.sleep(5);
        gameData = DriverStation.getInstance().getGameSpecificMessage();
        if (gameData == null) {
          gameData = "";
        }
      } catch (Exception e) {
      }
      retries--;
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
