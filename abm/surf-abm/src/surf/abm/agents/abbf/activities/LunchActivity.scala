package surf.abm.agents.abbf.activities

import surf.abm.agents.{Agent, UrbanAgent}
import surf.abm.agents.abbf.{ABBFAgent, Place, TimeProfile}
import surf.abm.agents.abbf.activities.ActivityTypes.LUNCHING
import surf.abm.main.SurfABM


/**
  * An activity (of type [[surf.abm.agents.abbf.activities.ActivityTypes.LUNCHING]]) that causes the agent to
  * travel to lunch places
  */
case class LunchActivity(
                         override val timeProfile: TimeProfile,
                         override val agent: ABBFAgent,
                         override val place: Place)
  extends FixedActivity(LUNCHING, timeProfile, agent, place)  with Serializable
// for initial tests: fixed activity most nearby to work
// should be flexible activity in the future
{

  // These variables define the different things that the agent could be doing in order to satisfy the work activity
  // (Note: in SleepActivity, these are defined as case classes that extend a sealed trait, but this way is probably
  // more efficient)

  private val LUNCHING = 1
  private val TRAVELLING = 2
  private val INITIALISING = 3
  private var currentAction = INITIALISING
  //private val place : Place = null // start with a null place

  /**
    * This makes the agent actually perform the activity.
    *
    * @return True if the agent has performed this activity, false if they have not (e.g. if they are still travelling
    *         to a particular destination).
    */
  override def performActivity(): Boolean = {

    this.currentAction match {

      case INITIALISING => {
        Agent.LOG.debug(agent, "is initialising LunchActivity")
        // See if the agent is in a lunch place
        if (this.place.location.equalLocation(
          this.agent.location())) {
          Agent.LOG.debug(agent, "as reached a lunch place. Starting lunch.")
          currentAction = LUNCHING // Next iteration the agent will start to have lunch.
        }
        else {
          Agent.LOG.debug(agent, "is not at a lunch place yet. Travelling there.")
          this.agent.newDestination(Option(this.place.location))
          currentAction = TRAVELLING
        }

      }

      case TRAVELLING => {
        if (this.agent.atDestination()) {
          Agent.LOG.debug(agent, "has reached a lunch place. Starting lunch")
          currentAction = LUNCHING
        }
        else {
          Agent.LOG.debug(agent, "is travelling to a lunch place.")
          agent.moveAlongPath()
        }
      }

      case LUNCHING => {
        Agent.LOG.debug(agent, "is having lunch")
        return true
      }

    }
    // Only get here if the agent isn't lunching, so must return false.
    return false

  }

  override def activityChanged(): Unit = {
    this.currentAction = INITIALISING
    this._currentIntensityDecrease = 0d
    //throw new NotImplementedError("Have not implemented Shopping activity yet")
  }

  /**
    * The amount that the lunch activity should increase at each iteration
    * @return
    */
  override def backgroundIncrease(): Double = {
    return 1d / (10d * SurfABM.ticksPerDay)
  }

  /**
    * The amount that the lunch activity will go down by if an agent is lunching.
    * @return
    */
  override def reduceActivityAmount(): Double = {
    return 100d / (3d * SurfABM.ticksPerDay)
  }
}
