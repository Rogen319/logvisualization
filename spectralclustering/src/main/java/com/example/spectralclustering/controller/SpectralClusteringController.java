package com.example.spectralclustering.controller;

import com.example.spectralclustering.dto.ClusterResult;
import com.example.spectralclustering.service.SpectralClusteringSerivce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cluster")
public class SpectralClusteringController {

    @Autowired
    private SpectralClusteringSerivce spectralClusteringSerivce;

    @CrossOrigin(origins = "*")
    @GetMapping()
    public ClusterResult getClusterResult(@RequestParam(required = true) long endTs,
    		@RequestParam(required = true) long lookback, @RequestParam(required = true) int k){
    		return spectralClusteringSerivce.getResult(endTs, lookback, k);
    }
}
