package algrithm.spectralclustering.service;

import algrithm.spectralclustering.dto.ClusterResult;

public interface SpectralClusteringSerivce {
    ClusterResult getResult(long endTs, long lookback, int k);
}
