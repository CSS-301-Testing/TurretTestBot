
package org.usfirst.frc.team2811.robot;

import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.RobotDrive;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


/**
 * This is a demo program showing the use of the RobotDrive class.
 * The SampleRobot class is the base of a robot application that will automatically call your
 * Autonomous and OperatorControl methods at the right time as controlled by the switches on
 * the driver station or the field controls.
 *
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SampleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 *
 * WARNING: While it may look like a good choice to use for your code if you're inexperienced,
 * don't. Unless you know what you are doing, complex code will be much more difficult under
 * this system. Use IterativeRobot or Command-Based instead if you're new.
 */

public class Robot extends SampleRobot {
    RobotDrive myRobot;
    Joystick stick;
    Button turnClock;
    Button turnCounterClock;
    CANTalon turretMotor;
    final String defaultAuto = "Default";
    final String customAuto = "My Auto";
    SendableChooser chooser;
    
    
    int upTicks = 12339; //RevLimitSwitch
    int downTicks = -2670; //FwdLimitSwitch
    
    int upAngle = 180;
    int downAngle = 0;
    
    int downJoystick = -1;
    int upJoystick = 1;
    boolean encPosSet = false;
    boolean upTicksSet = false;
    boolean downTicksSet = false;
    int targetAngle = 90;
    
    MiniPID pid;
    
    public Robot() {
        myRobot = new RobotDrive(0, 1);
        myRobot.setExpiration(0.1);
        stick = new Joystick(0);
        turnClock = new JoystickButton(stick,1);
        turnCounterClock = new JoystickButton(stick,2);
        turretMotor = new CANTalon(4);
        pid = new MiniPID(0.05,0,0);
        pid.setOutputLimits(-1, 1);
        pid.setDirection(true);
    }
    
    public void robotInit() {
        chooser = new SendableChooser();
        chooser.addDefault("Default Auto", defaultAuto);
        chooser.addObject("My Auto", customAuto);
        SmartDashboard.putData("Auto modes", chooser);
        turretMotor.reset();
    	turretMotor.clearStickyFaults();
    	turretMotor.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
    	turretMotor.enable();
    	turretMotor.set(0);
    }

	/**
	 * This autonomous (along with the chooser code above) shows how to select between different autonomous modes
	 * using the dashboard. The sendable chooser code works with the Java SmartDashboard. If you prefer the LabVIEW
	 * Dashboard, remove all of the chooser code and uncomment the getString line to get the auto name from the text box
	 * below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the if-else structure below with additional strings.
	 * If using the SendableChooser make sure to add them to the chooser code above as well.
	 */
    public void autonomous() {
    	
    	String autoSelected = (String) chooser.getSelected();
//		String autoSelected = SmartDashboard.getString("Auto Selector", defaultAuto);
		System.out.println("Auto selected: " + autoSelected);
    	
    	switch(autoSelected) {
    	case customAuto:
            myRobot.setSafetyEnabled(false);
            myRobot.drive(-0.5, 1.0);	// spin at half speed
            Timer.delay(2.0);		//    for 2 seconds
            myRobot.drive(0.0, 0.0);	// stop robot
            break;
    	case defaultAuto:
    	default:
            myRobot.setSafetyEnabled(false);
            myRobot.drive(-0.5, 0.0);	// drive forwards half speed
            Timer.delay(2.0);		//    for 2 seconds
            myRobot.drive(0.0, 0.0);	// stop robot
            break;
    	}
    }

    /**
     * Runs the motors with arcade steering.
     */
    public void operatorControl() {
    	while(isOperatorControl()&&isEnabled()){
        	Timer.delay(0.005);

//    		manualTurn();
    		if(!encPosSet){
    			twoWayHoming();
    		}else{
    			myRobot.setSafetyEnabled(true);
    			System.out.println("upTicks: " + upTicks);
    			System.out.println("downTicks: " + downTicks);
//    			System.out.println(ticksToAngle(turretMotor.getEncPosition()));
//    			System.out.print(joystickToAngle(getJoystickAngle(stick)));
//    			double output = pid.getOutput(ticksToAngle(turretMotor.getEncPosition()), joystickToAngle(getJoystickAngle(stick)));
//    	    	System.out.println(output);
    			//manualTurn();
    	        autoTurn(joystickToAngle(getJoystickAngle(stick)));
    		}
    	}
    }
       

    /**
     * Runs during test mode
     */
    public void test() {
    }
    
    public double getJoystickAngle(Joystick joystick){
		//read joystick here
		return (joystick.getRawAxis(2));
	}
    
    public double ticksToAngle(int ticks){
    	return map(ticks, downTicks, upTicks, downAngle, upAngle);
    }
    
    public double joystickToAngle(double input){
    	return map(input, downJoystick, upJoystick, downAngle, upAngle);
    }
    
    public int angleToTicks(double angle){
    	return (int)map(angle, downAngle, upAngle, downTicks, upTicks);
    }
    
    public double map(double x, double in_min, double in_max, double out_min, double out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
    
    public void manualTurn(){
    	System.out.println("encPos:" + turretMotor.getEncPosition());
        if(turnClock.get()){        	
        	turretMotor.set(0.2);
        }else if(turnCounterClock.get()){
        	turretMotor.set(-0.2);
        }else{
        	turretMotor.set(0);
        }
    }
    
    
    public void oneWayHoming(){
    	turretMotor.set(-0.2); //Stop when run counter clockwise
    	if(turretMotor.isRevLimitSwitchClosed()){
    		turretMotor.setEncPosition(upTicks);
    		encPosSet = true;
    	}
    }
    
    public void twoWayHoming(){
    	if(!upTicksSet){
    		turretMotor.set(-0.3); //Stop when run counter clockwise
    		if(turretMotor.isRevLimitSwitchClosed()){
    			upTicks = turretMotor.getEncPosition();
    			upTicksSet = true;
    			System.out.println("upTicksSet " + upTicks);
    		}
    	}else if(!downTicksSet){
    		turretMotor.set(0.3);
    		if(turretMotor.isFwdLimitSwitchClosed()){
    			downTicks = turretMotor.getEncPosition();
    			downTicksSet = true;
    			System.out.println("downTicksSet " + downTicks);
    		}
    	}else if(upTicksSet&&downTicksSet){
    		encPosSet = true;
    	}
    }
    
    //Starts at 0, motor should set to be positive
    public void autoTurn(double angle){
    	double outPut = pid.getOutput(ticksToAngle(turretMotor.getEncPosition()), angle);
    	System.out.println(outPut);
    	turretMotor.set(-outPut);
    }
    
}
