package com.ghostipedia.nebulaeae2.optimizer;

public interface OptimizerMenu {
    OptimizerSnapshot nebulae$optimizerSnapshot();

    void nebulae$optimizerAction(OptimizerRequest request);

    void nebulae$closeOptimizer();
}
