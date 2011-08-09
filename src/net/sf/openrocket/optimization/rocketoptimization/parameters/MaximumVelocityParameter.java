package net.sf.openrocket.optimization.rocketoptimization.parameters;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.rocketoptimization.OptimizableParameter;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.exception.MotorIgnitionException;
import net.sf.openrocket.simulation.exception.SimulationCancelledException;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.exception.SimulationLaunchException;
import net.sf.openrocket.simulation.listeners.system.ApogeeEndListener;
import net.sf.openrocket.simulation.listeners.system.InterruptListener;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

/**
 * An optimization parameter that computes the maximum velocity during a simulated flight.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MaximumVelocityParameter implements OptimizableParameter {
	
	private static final LogHelper log = Application.getLogger();
	private static final Translator trans = Application.getTranslator();
	
	@Override
	public String getName() {
		return trans.get("name");
	}
	
	@Override
	public double computeValue(Simulation simulation) throws OptimizationException, InterruptedException {
		try {
			log.debug("Running simulation to evaluate maximum velocity");
			simulation.simulate(new ApogeeEndListener(), new InterruptListener());
			double value = simulation.getSimulatedData().getBranch(0).getMaximum(FlightDataType.TYPE_VELOCITY_TOTAL);
			log.debug("Maximum velocity was " + value);
			return value;
		} catch (MotorIgnitionException e) {
			// A problem with motor ignition will cause optimization to fail
			throw new OptimizationException(e);
		} catch (SimulationLaunchException e) {
			// Other launch exceptions result in zero velocity
			return Double.NaN;
		} catch (SimulationCancelledException e) {
			throw (InterruptedException) new InterruptedException("Optimization was interrupted").initCause(e);
		} catch (SimulationException e) {
			// Other exceptions fail
			throw new OptimizationException(e);
		}
	}
	
	@Override
	public UnitGroup getUnitGroup() {
		return UnitGroup.UNITS_VELOCITY;
	}
	
}
