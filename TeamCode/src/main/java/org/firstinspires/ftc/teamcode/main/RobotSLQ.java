package org.firstinspires.ftc.teamcode.main;

import org.ejml.simple.SimpleMatrix;
import org.firstinspires.ftc.teamcode.debugging.ComputerDebugger;
import org.firstinspires.ftc.teamcode.debugging.IllegalMessageTypeException;
import org.firstinspires.ftc.teamcode.debugging.MessageOption;
import org.firstinspires.ftc.teamcode.lib.control.MecanumDriveMPC;
import org.firstinspires.ftc.teamcode.lib.control.MecanumDriveSLQ;
import org.firstinspires.ftc.teamcode.lib.control.MecanumRunnableSLQ;
import org.firstinspires.ftc.teamcode.lib.control.Obstacle;
import org.firstinspires.ftc.teamcode.lib.control.Waypoint;
import org.firstinspires.ftc.teamcode.lib.geometry.Circle2d;
import org.firstinspires.ftc.teamcode.lib.geometry.Line2d;
import org.firstinspires.ftc.teamcode.lib.geometry.Pose2d;
import org.firstinspires.ftc.teamcode.lib.geometry.Rotation2d;
import org.firstinspires.ftc.teamcode.lib.geometry.Translation2d;
import org.firstinspires.ftc.teamcode.lib.util.TimeUnits;
import org.firstinspires.ftc.teamcode.lib.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class RobotSLQ extends Robot {
    private static List<Pose2d> positions   = new ArrayList<>();
    private static List<Obstacle> obstacles = new ArrayList<>();
    private static List<Waypoint> waypoints = new ArrayList<>();

    static {
        //positions.add(new Pose2d(120, 120, new Rotation2d(Math.toRadians(180d), false)));
        positions.add(new Pose2d(100d, 51d, new Rotation2d(Math.toRadians(-135d), false)));
        positions.add(new Pose2d(104d, 120d, new Rotation2d(Math.toRadians(0d), false)));
        positions.add(new Pose2d(120d, 116d, new Rotation2d(Math.toRadians(-90d), false)));
        positions.add(new Pose2d(100d, 26d, new Rotation2d(Math.toRadians(-135), false)));
        positions.add(new Pose2d(106d, 116d, new Rotation2d(Math.toRadians(-90d), false)));
        positions.add(new Pose2d(102d, 42d, new Rotation2d(Math.toRadians(-135), false)));
        positions.add(new Pose2d(106d, 116d, new Rotation2d(Math.toRadians(-90d), false)));
        positions.add(new Pose2d(102d, 34d, new Rotation2d(Math.toRadians(-135), false)));
        positions.add(new Pose2d(106d, 116d, new Rotation2d(Math.toRadians(-90d), false)));
        positions.add(new Pose2d(102d, 18d, new Rotation2d(Math.toRadians(-135), false)));
        positions.add(new Pose2d(106d, 116d, new Rotation2d(Math.toRadians(-90d), false)));
        positions.add(new Pose2d(102d, 12d, new Rotation2d(Math.toRadians(-135), false)));
        positions.add(new Pose2d(106d, 116d, new Rotation2d(Math.toRadians(-90d), false)));
        positions.add(new Pose2d(110d, 72d, new Rotation2d(Math.toRadians(-90d), false)));

        obstacles.add(new Obstacle(92d, 65d, 3d, 25d));
        obstacles.add(new Obstacle(92d, 80d, 3d, 100d));
        obstacles.add(new Obstacle(144d - 9d, 90d, 9d, 200d));

        //obstacles.add(new Obstacle(88d, 45d, 3d, 0.5d));
        //obstacles.add(new Obstacle(67d, 51d, 3d, 1d));

        /*waypoints.add(new Waypoint(new SimpleMatrix(6, 6, true, new double[] {
                10000, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0,
                0, 0, 10000, 0, 0, 0,
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0
        }), 1d, 1.2d, new Pose2d(
                36, 50, new Rotation2d(Math.toRadians(360d), false)
        )));*/
    }

    @Override
    public void init_debug() {
        super.init_debug();
        setMecanumDriveMPC(new MecanumDriveMPC(getDriveModel()));
        setMecanumDriveSLQ(new MecanumDriveSLQ(getMecanumDriveMPC()));

        getMecanumDriveSLQ().initialIteration(getState(), positions.get(0));
        for(int i = 0; i < MecanumRunnableSLQ.getMaxIterations(); i++) {
            getMecanumDriveSLQ().simulateIteration(getState(), positions.get(0));
            getMecanumDriveSLQ().runSLQ();
        }

        setMecanumRunnableSLQ(new MecanumRunnableSLQ());
        getMecanumRunnableSLQ().setDesiredState(positions.get(0));
        new Thread(getMecanumRunnableSLQ()).start();
    }

    @Override
    public void loop_debug() {
        super.loop_debug();
        getMecanumRunnableSLQ().updateSLQ();
        setInput(getMecanumDriveSLQ().getOptimalInput((int)((getMecanumRunnableSLQ().getTimeProfiler().getDeltaTime(TimeUnits.SECONDS, false) +
                getMecanumRunnableSLQ().getPolicyLag()) / MecanumDriveMPC.getDt()), getState(), 0.001d));

        if(getFieldPosition().getTranslation().epsilonEquals(positions.get(0).getTranslation(), 6d) && positions.size() > 1) {
            positions.remove(0);
            getMecanumRunnableSLQ().setDesiredState(positions.get(0));
        } else if(getFieldPosition().getTranslation().epsilonEquals(positions.get(0).getTranslation(), 1d) && positions.size() == 1) {
            stopTimer();
            setInput(new SimpleMatrix(4, 1, true, new double[] {
                    0, 0, 0, 0
            }));
        }

        try {
            for(int i = 0; i < getMecanumDriveSLQ().getSimulatedStates().length - 1; i++) {
                ComputerDebugger.send(MessageOption.LINE.setSendValue(
                        new Line2d(new Translation2d(
                                getMecanumDriveSLQ().getSimulatedStates()[i].get(0) / 0.0254d,
                                getMecanumDriveSLQ().getSimulatedStates()[i].get(2) / 0.0254d
                        ), new Translation2d(
                                getMecanumDriveSLQ().getSimulatedStates()[i + 1].get(0) / 0.0254d,
                                getMecanumDriveSLQ().getSimulatedStates()[i + 1].get(2) / 0.0254d
                        ))
                ));
            }

            for(int i = 0; i < positions.size(); i++) {
                //ComputerDebugger.send(MessageOption.KEY_POINT.setSendValue(positions.get(i).getTranslation()));
            }

            for(int j = 0; j < getObstacles().size(); j++) {
                ComputerDebugger.send(MessageOption.KEY_POINT.setSendValue(new Circle2d(
                        getObstacles().get(j).getLocation(), getObstacles().get(j).getObstacleRadius() / 0.0254d
                )));
            }

            for(int j = 0; j < getWaypoints().size(); j++) {
                if(getWaypoints().get(j).getDesiredTime() /*+ getWaypoints().get(j).getTemporalSpread()*/ < TimeUtil.getCurrentRuntime(TimeUnits.SECONDS)) {
                    getWaypoints().remove(j--);
                }

                if(!getWaypoints().isEmpty() && j >= 0) {
                    ComputerDebugger.send(MessageOption.KEY_POINT.setSendValue(getWaypoints().get(j).getLocation()));
                }
            }
        } catch (IllegalMessageTypeException e) {
            e.printStackTrace();
        }
    }

    public static List<Obstacle> getObstacles() {
        return obstacles;
    }

    public static List<Waypoint> getWaypoints() {
        return waypoints;
    }
}
