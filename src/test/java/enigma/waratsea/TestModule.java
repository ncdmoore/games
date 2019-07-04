package enigma.waratsea;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.aircraft.AircraftFactory;
import engima.waratsea.model.aircraft.AircraftImpl;
import engima.waratsea.model.aircraft.Fighter;
import engima.waratsea.model.aircraft.PoorNavalBomber;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldFactory;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.base.port.PortFactory;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.flotilla.FlotillaFactory;
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
import engima.waratsea.model.player.ComputerPlayer;
import engima.waratsea.model.player.HumanPlayer;
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
import engima.waratsea.model.target.TargetFactory;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceFactory;
import engima.waratsea.model.victory.AirfieldVictory;
import engima.waratsea.model.victory.AirfieldVictoryFactory;
import engima.waratsea.model.victory.RequiredShipVictory;
import engima.waratsea.model.victory.ShipVictory;
import engima.waratsea.model.victory.SquadronVictory;
import engima.waratsea.model.victory.SquadronVictoryFactory;
import engima.waratsea.model.victory.VictoryCondition;
import engima.waratsea.model.victory.VictoryConditions;
import engima.waratsea.model.victory.VictoryConditionsFactory;
import engima.waratsea.model.victory.ShipVictoryFactory;
import engima.waratsea.model.victory.data.AirfieldVictoryData;
import engima.waratsea.model.victory.data.ShipVictoryData;
import engima.waratsea.model.victory.data.SquadronVictoryData;

public class TestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Player.class).annotatedWith(Names.named("Human")).to(HumanPlayer.class);
        bind(Player.class).annotatedWith(Names.named("Computer")).to(ComputerPlayer.class);

        install(new FactoryModuleBuilder().implement(TaskForce.class, TaskForce.class).build(TaskForceFactory.class));
        install(new FactoryModuleBuilder()
                .implement(Ship.class, Names.named("aircraft"), AircraftCarrier.class)
                .implement(Ship.class, Names.named("surface"), SurfaceShip.class)
                .build(ShipFactory.class));

        install(new FactoryModuleBuilder().implement(Flotilla.class, Flotilla.class).build(FlotillaFactory.class));
        install(new FactoryModuleBuilder().implement(FlotillaDeployment.class, FlotillaDeployment.class).build(FlotillaDeploymentFactory.class));

        install(new FactoryModuleBuilder().implement(Submarine.class, Submarine.class).build(SubmarineFactory.class));

        install(new FactoryModuleBuilder().implement(Allotment.class, Allotment.class).build(AllotmentFactory.class));
        install(new FactoryModuleBuilder().implement(AllotmentTable.class, AllotmentTable.class).build(AllotmentTableFactory.class));

        install(new FactoryModuleBuilder().implement(SquadronDeployment.class, SquadronDeployment.class).build(SquadronDeploymentFactory.class));

        install(new FactoryModuleBuilder().implement(Squadron.class, Squadron.class).build(SquadronFactory.class));
        install(new FactoryModuleBuilder()
                .implement(Aircraft.class, Names.named("aircraft"), AircraftImpl.class)
                .implement(Aircraft.class, Names.named("fighter"), Fighter.class)
                .implement(Aircraft.class, Names.named("poorNaval"), PoorNavalBomber.class)
                .build(AircraftFactory.class));

        install(new FactoryModuleBuilder().implement(Region.class, Region.class).build(RegionFactory.class));

        install(new FactoryModuleBuilder().implement(VictoryConditions.class, VictoryConditions.class).build(VictoryConditionsFactory.class));

        install(new FactoryModuleBuilder()
              .implement(new TypeLiteral<VictoryCondition<ShipEvent, ShipVictoryData>>(){}, Names.named("ship"), ShipVictory.class)
              .implement(new TypeLiteral<VictoryCondition<ShipEvent, ShipVictoryData>>(){}, Names.named("required"), RequiredShipVictory.class)
              .build(new TypeLiteral<ShipVictoryFactory<ShipEvent, ShipVictoryData>>(){}));

        install(new FactoryModuleBuilder()
                .implement(new TypeLiteral<VictoryCondition<SquadronEvent, SquadronVictoryData>>() { }, Names.named("squadron"), SquadronVictory.class)
                .build(new TypeLiteral<SquadronVictoryFactory<SquadronEvent, SquadronVictoryData>>() { }));

        install(new FactoryModuleBuilder()
                .implement(new TypeLiteral<VictoryCondition<AirfieldEvent, AirfieldVictoryData>>() { }, Names.named("airfield"), AirfieldVictory.class)
                .build(new TypeLiteral<AirfieldVictoryFactory<AirfieldEvent, AirfieldVictoryData>>() { }));


        install(new FactoryModuleBuilder().implement(Airfield.class, Airfield.class).build(AirfieldFactory.class));
        install(new FactoryModuleBuilder().implement(Port.class, Port.class).build(PortFactory.class));

        install(new FactoryModuleBuilder().implement(Minefield.class, Minefield.class).build(MinefieldFactory.class));
        install(new FactoryModuleBuilder().implement(MinefieldZone.class, MinefieldZone.class).build(MinefieldZoneFactory.class));
        install(new FactoryModuleBuilder().implement(MinefieldDeployment.class, MinefieldDeployment.class).build(MinefieldDeploymentFactory.class));

        install(new FactoryModuleBuilder().implement(Target.class, Target.class).build(TargetFactory.class));

        install(new FactoryModuleBuilder().implement(ShipEventMatcher.class, ShipEventMatcher.class).build(ShipEventMatcherFactory.class));
        install(new FactoryModuleBuilder().implement(SquadronEventMatcher.class, SquadronEventMatcher.class).build(SquadronEventMatcherFactory.class));
        install(new FactoryModuleBuilder().implement(AirfieldEventMatcher.class, AirfieldEventMatcher.class).build(AirfieldEventMatcherFactory.class));
    }
}
