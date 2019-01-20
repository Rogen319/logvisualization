package algrithm.spectralclustering.service.impl;

import algrithm.spectralclustering.config.ZipkinConfig;
import algrithm.spectralclustering.enumeration.ServiceExcludeEnum;
import algrithm.spectralclustering.dto.ClusterResult;
import algrithm.spectralclustering.dto.SingleDependency;
import algrithm.spectralclustering.service.SpectralClusteringSerivce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Logger log = LoggerFactory.getLogger(this.getClass());

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
//        log.info(String.format("The dependencyList is %s", dependencyList));
        //打印矩阵
        double[][] martix = convJson2array(dependencyList);
//        printMatrix(martix);
        SpectralClustering sc = new SpectralClustering((convCallCount2Proportion(martix)), k);
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

    private void printMatrix(double[][] martix) {
        for(int i = 0; i < martix.length; i++) {
            for(int j = 0; j < martix[0].length; j++) {
                System.out.print(martix[i][j] + "   ");
            }
            System.out.println();
        }
    }


    private List<SingleDependency> getZipkinDependencies(String url) {

        RestTemplate restTemplate = new RestTemplate();
        SingleDependency[] dependencies =
                restTemplate.getForObject(url, SingleDependency[].class);
        if (null == dependencies) {
            throw new RuntimeException("Fail to get dependency data");
        }

        final List<String> serviceExclude = new ArrayList<>();
        for (ServiceExcludeEnum se : ServiceExcludeEnum.values()) {
            serviceExclude.add(se.getServiceName());
        }

        if(dependencies.length == 0) {
            return new ArrayList<>();
        }

        Stream<SingleDependency> dependencyStream = Stream.of(dependencies);
        return dependencyStream.filter(x -> !serviceExclude.contains(x.getParent()) || !serviceExclude.contains(x.getChild())).collect(Collectors.toList());
    }

    private double[][] convJson2array(List<SingleDependency> dependenciesList) {
        List<String> services = getServices(dependenciesList);
//        log.info(String.format("The service list is %s", services));
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
            if(abscissa == ordinate){
                callCountMatrix[abscissa][ordinate] = 0;
                continue;
            }
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



