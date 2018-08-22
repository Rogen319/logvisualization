package algrithm.spectralclustering.service.impl;

import algrithm.spectralclustering.config.ZipkinConfig;
import algrithm.spectralclustering.enumeration.ServiceExclude;
import algrithm.spectralclustering.dto.ClusterResult;
import algrithm.spectralclustering.dto.SingleDependency;
import algrithm.spectralclustering.service.SpectralClusteringSerivce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import smile.clustering.SpectralClustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

@Service
public class SpectralClusteringServiceImpl implements SpectralClusteringSerivce {
    @Autowired
    private ZipkinConfig zipkinConfig;

    @Override
    public ClusterResult getResult(long endTs, long lookback, int k) {

        StringBuilder sb = new StringBuilder("http://");
        sb.append(zipkinConfig.getIp())
        .append(":")
        .append(zipkinConfig.getPort())
        .append("/zipkin/api/v2/dependencies?endTs=")
        .append(endTs)
        .append("&lookback=")
        .append(lookback);

        List<SingleDependency> dependencyList = getZipkinDependencies(sb.toString());
        SpectralClustering sc = new SpectralClustering((convCallCount2Proportion(convJson2array(dependencyList))), k);
        List<String> services = getServices(dependencyList);

        ClusterResult clusterResult = new ClusterResult();
        clusterResult.setResult(sc.toString());
        clusterResult.setServices(services);
        clusterResult.setClusterSize(Arrays.toString(sc.getClusterSize()));
        clusterResult.setClusterCount(k);

        List<List<String>> clusters = new ArrayList<>();
        int[] lables = sc.getClusterLabel();
        for (int i = 0; i < sc.getClusterSize().length; i++) {
            List<String> clusterServices = new ArrayList<>();
            for (int j = 0; j < services.size(); j++) {
                if (lables[j] == i) {
                    clusterServices.add(services.get(j));
                }
            }
            clusters.add(clusterServices);
        }

        clusterResult.setClusters(clusters);

        return clusterResult;
    }


    private List<SingleDependency> getZipkinDependencies(String url) {

        RestTemplate restTemplate = new RestTemplate();
        SingleDependency[] dependencies =
                restTemplate.getForObject(url, SingleDependency[].class);
        if (null == dependencies || 0 ==
                dependencies.length) {
            throw new RuntimeException("Fail to get dependency data");
        }

        final List<String> serviceExclude = new ArrayList<>();
        for (ServiceExclude se : ServiceExclude.values()) {
            serviceExclude.add(se.getServiceName());
        }

        Stream<SingleDependency> dependencyStream = Stream.of(dependencies);
        return dependencyStream.filter(x -> !serviceExclude.contains(x.getParent()) || !serviceExclude.contains(x.getChild())).collect(Collectors.toList());
    }

    private double[][] convJson2array(List<SingleDependency> dependenciesList) {


        List<String> services = getServices(dependenciesList);


        double[][] callCountMatrix = new double[services.size()][services.size()];

        for (SingleDependency dependency :
                dependenciesList) {
            String parent = dependency.getParent();
            String child = dependency.getChild();

            if (!services.contains(parent)) {
                services.add(parent);
            }

            if (!services.contains(child)) {
                services.add(child);
            }
            int abscissa = services.indexOf(parent);
            int ordinate = services.indexOf(child);
            callCountMatrix[abscissa][ordinate] = dependency.getCallCount();
            callCountMatrix[ordinate][abscissa] = dependency.getCallCount();

        }

        return callCountMatrix;
    }

    private List<String> getServices(List<SingleDependency> dependenciesList) {

        List<String> services = new ArrayList<>();
        for (SingleDependency dependency :
                dependenciesList) {
            String parent = dependency.getParent();
            String child = dependency.getChild();

            if (!services.contains(parent)) {
                services.add(parent);
            }

            if (!services.contains(child)) {
                services.add(child);
            }

        }

        return services;
    }

    private double[][] convCallCount2Proportion(double[][] callCountMatrix) {
        int len = callCountMatrix.length;
        for (int i = 0; i < len; i++) {
            double sum = DoubleStream.of(callCountMatrix[i]).sum();
            for (int j = 0; j > i && j < len; j++) {
                if (callCountMatrix[i][j] != 0) {
                    callCountMatrix[i][j] = callCountMatrix[i][j] / sum;
                    callCountMatrix[j][i] = callCountMatrix[j][i] / sum;
                }
            }
        }
        return callCountMatrix;
    }
}



