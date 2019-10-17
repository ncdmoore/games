package enigma.waratsea.model.squadron.state;

import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.squadron.state.SquadronState;
import org.junit.Assert;
import org.junit.Test;

import static engima.waratsea.model.squadron.state.SquadronState.*;

public class SquadronStateTest {

    @Test
    public void readyTransistionTest() {
        SquadronState squadronState = READY;

        squadronState = squadronState.transition(SquadronAction.ASSIGN_TO_PATROL);

        Assert.assertSame(squadronState, QUEUED_FOR_PATROL);
    }

    @Test
    public void readyInvalidTransitionTest() {
        SquadronState squadronState = READY;

        squadronState = squadronState.transition(SquadronAction.RETURN);

        Assert.assertSame(squadronState, READY);
    }

    @Test
    public void queuedForPatrolTest() {
        SquadronState squadronState = QUEUED_FOR_PATROL;

        squadronState = squadronState.transition(SquadronAction.REMOVE_FROM_PATROL);

        Assert.assertSame(squadronState, READY);
    }

    @Test
    public void queuedForMissionTest() {
        SquadronState squadronState = QUEUED_FOR_MISSION;

        squadronState = squadronState.transition(SquadronAction.REMOVE_FROM_MISSION);

        Assert.assertSame(squadronState, READY);
    }

    @Test
    public void onPatrolTransitionTest() {
        SquadronState squadronState = ON_PATROL;

        squadronState = squadronState.transition(SquadronAction.REMOVE_FROM_PATROL);

        Assert.assertSame(squadronState, HANGER);
    }

    @Test
    public void onMissionInvalidTransitionTest() {
        SquadronState squadronState = ON_MISSION;

        squadronState = squadronState.transition(SquadronAction.ASSIGN_TO_PATROL);

        Assert.assertSame(squadronState, ON_MISSION);
    }

    @Test
    public void inHangerTransitionTest() {
        SquadronState squadronState = HANGER;

        squadronState = squadronState.transition(SquadronAction.REFIT);

        Assert.assertSame(squadronState, READY);
    }

}
