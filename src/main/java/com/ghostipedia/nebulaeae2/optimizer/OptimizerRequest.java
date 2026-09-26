package com.ghostipedia.nebulaeae2.optimizer;

public record OptimizerRequest(String action, int index, String operation, long factor, long revision) {}
