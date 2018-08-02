package com.example.spectralclustering.service;

import com.example.spectralclustering.dto.ClusterResult;

public interface SpectralClusteringSerivce {
    ClusterResult getResult(long endTs, long lookback, int k);
}
