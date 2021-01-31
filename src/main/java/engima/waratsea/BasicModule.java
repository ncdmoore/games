package engima.waratsea;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.aircraft.AircraftFactory;
import engima.waratsea.model.aircraft.Bomber;
import engima.waratsea.model.aircraft.Fighter;
import engima.waratsea.model.aircraft.Recon;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldFactory;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.Ferry;
import engima.waratsea.model.base.airfield.mission.LandStrike;
import engima.waratsea.model.base.airfield.mission.NavalPortStrike;
import engima.waratsea.model.base.airfield.mission.SweepAirfield;
import engima.waratsea.model.base.airfield.mission.SweepPort;
import engima.waratsea.model.base.airfield.mission.rules.MissionAirRules;
import engima.waratsea.model.base.airfield.mission.rules.MissionAirStrikeRules;
import engima.waratsea.model.base.airfield.mission.rules.MissionAirSweepRules;
import engima.waratsea.model.base.airfield.patrol.AswPatrol;
import engima.waratsea.model.base.airfield.patrol.CapPatrol;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolFactory;
import engima.waratsea.model.base.airfield.patrol.SearchPatrol;
import engima.waratsea.model.base.airfield.patrol.rules.PatrolAirAswRules;
import engima.waratsea.model.base.airfield.patrol.rules.PatrolAirCapRules;
import engima.waratsea.model.base.airfield.patrol.rules.PatrolAirRules;
import engima.waratsea.model.base.airfield.patrol.rules.PatrolAirSearchRules;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.base.port.PortFactory;
import engima.waratsea.model.enemy.views.airfield.AirfieldView;
import engima.waratsea.model.enemy.views.airfield.AirfieldViewFactory;
import engima.waratsea.model.enemy.views.port.PortView;
import engima.waratsea.model.enemy.views.port.PortViewFactory;
import engima.waratsea.model.enemy.views.taskForce.TaskForceView;
import engima.waratsea.model.enemy.views.taskForce.TaskForceViewFactory;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.flotilla.FlotillaFactory;
import engima.waratsea.model.flotilla.MotorTorpedoBoatFlotilla;
import engima.waratsea.model.flotilla.SubmarineFlotilla;
import engima.waratsea.model.flotilla.deployment.FlotillaDeployment;
import engima.waratsea.model.flotilla.deployment.FlotillaDeploymentFactory;
import engima.waratsea.model.game.event.airfield.AirfieldEvent;
import engima.waratsea.model.game.event.airfield.AirfieldEventMatcher;
import engima.waratsea.model.game.event.airfield.AirfieldEventMatcherFactory;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.ship.ShipEventMatcherFactory;
import engima.waratsea.model.game.event.squadron.SquadronEvent;
import engima.waratsea.model.game.event.squadron.SquadronEventMatcher;
import engima.waratsea.model.game.event.squadron.SquadronEventMatcherFactory;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.map.region.RegionFactory;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.model.minefield.MinefieldFactory;
import engima.waratsea.model.minefield.deployment.MinefieldDeployment;
import engima.waratsea.model.minefield.deployment.MinefieldDeploymentFactory;
import engima.waratsea.model.minefield.zone.MinefieldZone;
import engima.waratsea.model.minefield.zone.MinefieldZoneFactory;
import engima.waratsea.model.motorTorpedoBoat.MotorTorpedoBoat;
import engima.waratsea.model.motorTorpedoBoat.MotorTorpedoBoatFactory;
import engima.waratsea.model.player.ComputerPlayer;
import engima.waratsea.model.player.HumanPlayer;
import engima.waratsea.model.player.NeutralPlayer;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.ship.AircraftCarrier;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.ship.ShipFactory;
import engima.waratsea.model.ship.SurfaceShip;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronFactory;
import engima.waratsea.model.squadron.allotment.Allotment;
import engima.waratsea.model.squadron.allotment.AllotmentFactory;
import engima.waratsea.model.squadron.allotment.AllotmentTable;
import engima.waratsea.model.squadron.allotment.AllotmentTableFactory;
import engima.waratsea.model.squadron.deployment.SquadronDeployment;
import engima.waratsea.model.squadron.deployment.SquadronDeploymentFactory;
import engima.waratsea.model.submarine.Submarine;
import engima.waratsea.model.submarine.SubmarineFactory;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetEnemyAirfield;
import engima.waratsea.model.target.TargetEnemyPort;
import engima.waratsea.model.target.TargetEnemyTaskForce;
import engima.waratsea.model.target.TargetFactory;
import engima.waratsea.model.target.TargetFriendlyAirfield;
import engima.waratsea.model.target.TargetFriendlyPort;
import engima.waratsea.model.target.TargetFriendlyTaskForce;
import engima.waratsea.model.target.TargetLandGrid;
import engima.waratsea.model.target.TargetSeaGrid;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceFactory;
import engima.waratsea.model.taskForce.mission.AirRaid;
import engima.waratsea.model.taskForce.mission.Bombardment;
import engima.waratsea.model.taskForce.mission.Escort;
import engima.waratsea.model.taskForce.mission.FerryAircraft;
import engima.waratsea.model.taskForce.mission.FerryShips;
import engima.waratsea.model.taskForce.mission.Intercept;
import engima.waratsea.model.taskForce.mission.Invasion;
import engima.waratsea.model.taskForce.mission.Minelaying;
import engima.waratsea.model.taskForce.mission.MissionFactory;
import engima.waratsea.model.taskForce.mission.SeaMission;
import engima.waratsea.model.taskForce.mission.StayInPort;
import engima.waratsea.model.taskForce.mission.Transport;
import engima.waratsea.model.victory.AirfieldVictory;
import engima.waratsea.model.victory.AirfieldVictoryFactory;
import engima.waratsea.model.victory.RequiredShipVictory;
import engima.waratsea.model.victory.ShipVictory;
import engima.waratsea.model.victory.ShipVictoryFactory;
import engima.waratsea.model.victory.SquadronVictory;
import engima.waratsea.model.victory.SquadronVictoryFactory;
import engima.waratsea.model.victory.VictoryCondition;
import engima.waratsea.model.victory.VictoryConditions;
import engima.waratsea.model.victory.VictoryConditionsFactory;
import engima.waratsea.model.victory.data.AirfieldVictoryData;
import engima.waratsea.model.victory.data.ShipVictoryData;
import engima.waratsea.model.victory.data.SquadronVictoryData;
import engima.waratsea.view.map.marker.main.BaseMarker;
import engima.waratsea.view.map.marker.main.BaseMarkerFactory;
import engima.waratsea.view.map.marker.main.RegionMarker;
import engima.waratsea.view.map.marker.main.TaskForceMarker;

/**
 * The guice basic module that configures the bindings for the main application.
 */
public class BasicModule extends AbstractModule {

    /**
     * Configure the main application guice model.
     */
    @Override
    protected void configure() {
        bind(Player.class).annotatedWith(Names.named("Human")).to(HumanPlayer.class);
        bind(Player.class).annotatedWith(Names.named("Computer")).to(ComputerPlayer.class);
        bind(Player.class).annotatedWith(Names.named("Neutral")).to(NeutralPlayer.class);

        install(new FactoryModuleBuilder().implement(TaskForce.class, TaskForce.class).build(TaskForceFactory.class));
        install(new FactoryModuleBuilder()
                .implement(Ship.class, Names.named("aircraft"), AircraftCarrier.class)
                .implement(Ship.class, Names.named("surface"), SurfaceShip.class)
                .build(ShipFactory.class));

        install(new FactoryModuleBuilder()
                .implement(Flotilla.class, Names.named("submarine"), SubmarineFlotilla.class)
                .implement(Flotilla.class, Names.named("mtb"), MotorTorpedoBoatFlotilla.class)
                .build(FlotillaFactory.class));

        install(new FactoryModuleBuilder().implement(FlotillaDeployment.class, FlotillaDeployment.class).build(FlotillaDeploymentFactory.class));

        install(new FactoryModuleBuilder().implement(Submarine.class, Submarine.class).build(SubmarineFactory.class));
        install(new FactoryModuleBuilder().implement(MotorTorpedoBoat.class, MotorTorpedoBoat.class).build(MotorTorpedoBoatFactory.class));


        install(new FactoryModuleBuilder().implement(Allotment.class, Allotment.class).build(AllotmentFactory.class));
        install(new FactoryModuleBuilder().implement(AllotmentTable.class, AllotmentTable.class).build(AllotmentTableFactory.class));

        install(new FactoryModuleBuilder().implement(SquadronDeployment.class, SquadronDeployment.class).build(SquadronDeploymentFactory.class));


        install(new FactoryModuleBuilder().implement(Squadron.class, Squadron.class).build(SquadronFactory.class));
        install(new FactoryModuleBuilder()
                .implement(Aircraft.class, Names.named("bomber"), Bomber.class)
                .implement(Aircraft.class, Names.named("fighter"), Fighter.class)
                .implement(Aircraft.class, Names.named("recon"), Recon.class)
                .build(AircraftFactory.class));


        install(new FactoryModuleBuilder().implement(Region.class, Region.class).build(RegionFactory.class));

        install(new FactoryModuleBuilder().implement(VictoryConditions.class, VictoryConditions.class).build(VictoryConditionsFactory.class));

        install(new FactoryModuleBuilder()
                .implement(new TypeLiteral<VictoryCondition<ShipEvent, ShipVictoryData>>() { }, Names.named("ship"), ShipVictory.class)
                .implement(new TypeLiteral<VictoryCondition<ShipEvent, ShipVictoryData>>() { }, Names.named("required"), RequiredShipVictory.class)
                .build(new TypeLiteral<ShipVictoryFactory<ShipEvent, ShipVictoryData>>() { }));

        install(new FactoryModuleBuilder()
                .implement(new TypeLiteral<VictoryCondition<SquadronEvent, SquadronVictoryData>>() { }, Names.named("squadron"), SquadronVictory.class)
                .build(new TypeLiteral<SquadronVictoryFactory<SquadronEvent, SquadronVictoryData>>() { }));

        install(new FactoryModuleBuilder()
                .implement(new TypeLiteral<VictoryCondition<AirfieldEvent, AirfieldVictoryData>>() { }, Names.named("airfield"), AirfieldVictory.class)
                .build(new TypeLiteral<AirfieldVictoryFactory<AirfieldEvent, AirfieldVictoryData>>() { }));


        install(new FactoryModuleBuilder().implement(Airfield.class, Airfield.class).build(AirfieldFactory.class));

        install(new FactoryModuleBuilder()
                .implement(AirMission.class, Names.named("ferry"), Ferry.class)
                .implement(AirMission.class, Names.named("landStrike"), LandStrike.class)
                .implement(AirMission.class, Names.named("navalPortStrike"), NavalPortStrike.class)
                .implement(AirMission.class, Names.named("sweepAirfield"), SweepAirfield.class)
                .implement(AirMission.class, Names.named("sweepPort"), SweepPort.class)
                .build(engima.waratsea.model.base.airfield.mission.MissionFactory.class));

        bind(MissionAirRules.class).annotatedWith(Names.named("airStrike")).to(MissionAirStrikeRules.class);
        bind(MissionAirRules.class).annotatedWith(Names.named("airSweep")).to(MissionAirSweepRules.class);


        install(new FactoryModuleBuilder()
                .implement(Patrol.class, Names.named("search"), SearchPatrol.class)
                .implement(Patrol.class, Names.named("asw"), AswPatrol.class)
                .implement(Patrol.class, Names.named("cap"), CapPatrol.class)
                .build(PatrolFactory.class));

        bind(PatrolAirRules.class).annotatedWith(Names.named("search")).to(PatrolAirSearchRules.class);
        bind(PatrolAirRules.class).annotatedWith(Names.named("asw")).to(PatrolAirAswRules.class);
        bind(PatrolAirRules.class).annotatedWith(Names.named("cap")).to(PatrolAirCapRules.class);

        install(new FactoryModuleBuilder().implement(Port.class, Port.class).build(PortFactory.class));

        install(new FactoryModuleBuilder().implement(Minefield.class, Minefield.class).build(MinefieldFactory.class));
        install(new FactoryModuleBuilder().implement(MinefieldZone.class, MinefieldZone.class).build(MinefieldZoneFactory.class));
        install(new FactoryModuleBuilder().implement(MinefieldDeployment.class, MinefieldDeployment.class).build(MinefieldDeploymentFactory.class));


        install(new FactoryModuleBuilder()
                .implement(SeaMission.class, Names.named("airRaid"), AirRaid.class)
                .implement(SeaMission.class, Names.named("bombardment"), Bombardment.class)
                .implement(SeaMission.class, Names.named("escort"), Escort.class)
                .implement(SeaMission.class, Names.named("ferry"), FerryShips.class)
                .implement(SeaMission.class, Names.named("ferryAircraft"), FerryAircraft.class)
                .implement(SeaMission.class, Names.named("intercept"), Intercept.class)
                .implement(SeaMission.class, Names.named("invasion"), Invasion.class)
                .implement(SeaMission.class, Names.named("minelaying"), Minelaying.class)
                .implement(SeaMission.class, Names.named("patrol"), engima.waratsea.model.taskForce.mission.Patrol.class)
                .implement(SeaMission.class, Names.named("stayInPort"), StayInPort.class)
                .implement(SeaMission.class, Names.named("transport"), Transport.class)
                .build(MissionFactory.class));

        install(new FactoryModuleBuilder()
                .implement(Target.class, Names.named("enemyAirfield"), TargetEnemyAirfield.class)
                .implement(Target.class, Names.named("friendlyAirfield"), TargetFriendlyAirfield.class)
                .implement(Target.class, Names.named("enemyPort"), TargetEnemyPort.class)
                .implement(Target.class, Names.named("friendlyPort"), TargetFriendlyPort.class)
                .implement(Target.class, Names.named("enemyTaskForce"), TargetEnemyTaskForce.class)
                .implement(Target.class, Names.named("friendlyTaskForce"), TargetFriendlyTaskForce.class)
                .implement(Target.class, Names.named("seaGrid"), TargetSeaGrid.class)
                .implement(Target.class, Names.named("landGrid"), TargetLandGrid.class)
                .build(TargetFactory.class));


        install(new FactoryModuleBuilder().implement(ShipEventMatcher.class, ShipEventMatcher.class).build(ShipEventMatcherFactory.class));
        install(new FactoryModuleBuilder().implement(SquadronEventMatcher.class, SquadronEventMatcher.class).build(SquadronEventMatcherFactory.class));
        install(new FactoryModuleBuilder().implement(AirfieldEventMatcher.class, AirfieldEventMatcher.class).build(AirfieldEventMatcherFactory.class));



        install(new FactoryModuleBuilder()
                .implement(BaseMarker.class, Names.named("base"), BaseMarker.class)
                .implement(TaskForceMarker.class, Names.named("taskforce"), TaskForceMarker.class)
                .implement(RegionMarker.class, Names.named("region"), RegionMarker.class)
                .build(BaseMarkerFactory.class));


        install(new FactoryModuleBuilder().implement(AirfieldView.class, AirfieldView.class).build(AirfieldViewFactory.class));
        install(new FactoryModuleBuilder().implement(PortView.class, PortView.class).build(PortViewFactory.class));
        install(new FactoryModuleBuilder().implement(TaskForceView.class, TaskForceView.class).build(TaskForceViewFactory.class));

    }
}


