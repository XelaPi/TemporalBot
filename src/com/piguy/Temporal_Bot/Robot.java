package com.piguy.Temporal_Bot;

import java.util.ArrayList;

/**
 * Represents any of the robots that can be controlled by the user on the board
 *
 * @author Alex Vanyo
 */
public class Robot extends Movable {

    private ArrayList<MoveCommand> moveCommands;

    public Robot(int x, int y, int direction, boolean visible) {
        super(x, y, direction, direction, visible);

        moveCommands = new ArrayList<MoveCommand>();
    }

    /**
     * Extends the reset function to include resetting all move commands associated with the robot
     */
    @Override
    public void reset() {
        super.reset();

        for (MoveCommand moveCommand : moveCommands) {
            moveCommand.reset();
        }
    }

    /**
     * Finds if a robot command needs to be run
     *
     * @param elapsedTime current time on the board
     * @return the move command that needs to be executed
     */
    public MoveCommand updateCommands(long elapsedTime) {
        for (MoveCommand moveCommand : moveCommands) {
            if (moveCommand.execute(elapsedTime)) {
                return moveCommand;
            }
        }

        return null;
    }

    /**
     * @return last move command for this robot
     */
    public MoveCommand getLastMoveCommand() {
        return moveCommands.isEmpty() ? null : moveCommands.get(moveCommands.size() - 1);
    }

    /**
     * @param command command that will be added to the robots list of commands
     */
    public void addMoveCommand(MoveCommand command) {
        moveCommands.add(command);
    }

    private static final String LOG_TAG = "Robot";
}
