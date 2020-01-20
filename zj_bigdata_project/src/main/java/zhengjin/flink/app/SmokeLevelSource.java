package zhengjin.flink.app;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * Flink SourceFunction to generate random SmokeLevel events.
 */
public class SmokeLevelSource implements SourceFunction<SmokeLevel> {

	private static final long serialVersionUID = 1L;
	private boolean running = true;

	@Override
	public void run(SourceContext<SmokeLevel> ctx) throws Exception {
		Random rand = new Random();

		while (this.running) {
			if (rand.nextGaussian() > 0.8) {
				ctx.collect(SmokeLevel.HIGH);
			} else {
				ctx.collect(SmokeLevel.LOW);
			}
			TimeUnit.SECONDS.wait(1L);
		}
	}

	@Override
	public void cancel() {
		this.running = false;
	}

}
