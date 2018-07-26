package com.thoughtmechanix.licenses.services;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.thoughtmechanix.licenses.clients.OrganizationFeignClient;
import com.thoughtmechanix.licenses.config.ServiceConfig;
import com.thoughtmechanix.licenses.model.License;
import com.thoughtmechanix.licenses.model.Organization;
import com.thoughtmechanix.licenses.repository.LicenseRepository;
import com.thoughtmechanix.licenses.repository.OrganizationRedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class LicenseService {

    private final static Logger logger = LoggerFactory.getLogger(LicenseService.class);

    @Autowired
    private ServiceConfig config;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private OrganizationFeignClient organizationFeignClient;

    @Autowired
    private OrganizationRedisRepository orgRedisRepo;

    private Organization checkRedisCache(String organizationId) {
        try {
            return orgRedisRepo.findOrganization(organizationId);
        } catch (Exception e) {
            logger.error("Error encountered while trying to retrieve organization {} check Redis Cache.Exception {}", organizationId, e);
            return null;
        }
    }

//    @HystrixCommand
    private Organization retrieveOrgInfo(String organizationId) {

        Organization org = checkRedisCache(organizationId);

        if (org != null) {
            logger.info("Successfully retrieved an organization {} from redis cache: {}", organizationId, org);
            return org;
        }

        Organization organization = organizationFeignClient.getOrganization(organizationId);
        cacheOrganization(organization);

        return organization;
    }

    public License getLicense(String organizationId, String licenseId) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);

        Organization org = retrieveOrgInfo(organizationId);

        return license
                .withOrganizationName( org.getName())
                .withContactName( org.getContactName())
                .withContactEmail( org.getContactEmail() )
                .withContactPhone( org.getContactPhone() );
    }

//    @HystrixCommand(
//            commandProperties=
//                    {@HystrixProperty(
//                            name="execution.isolation.thread.timeoutInMilliseconds",
//                            value="12000")})
//    @HystrixCommand(fallbackMethod = "buildFallbackLicenseList")
//    @HystrixCommand(fallbackMethod = "buildFallbackLicenseList",
//            commandProperties=
//                    {@HystrixProperty(
//                            name="execution.isolation.thread.timeoutInMilliseconds",
//                            value="10000")})
//    @HystrixCommand
    @HystrixCommand(//fallbackMethod = "buildFallbackLicenseList",
            threadPoolKey = "licenseByOrgThreadPool",
            threadPoolProperties =
                    {@HystrixProperty(name = "coreSize",value="30"),
                            @HystrixProperty(name="maxQueueSize", value="10")},
            commandProperties={
                    @HystrixProperty(name="circuitBreaker.requestVolumeThreshold", value="10"),
                    @HystrixProperty(name="circuitBreaker.errorThresholdPercentage", value="75"), // porcentagem de chamadas que devem falhar
                    @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds", value="7000"),
                    @HystrixProperty(name="metrics.rollingStats.timeInMilliseconds", value="15000"),
                    @HystrixProperty(name="metrics.rollingStats.numBuckets", value="5")}
    )
    public List<License> getLicensesByOrg(String organizationId){
        return licenseRepository.findByOrganizationId( organizationId );
    }

    public void saveLicense(License license){
        license.withId( UUID.randomUUID().toString());
        licenseRepository.save(license);
    }

    public void updateLicense(License license){
        licenseRepository.save(license);
    }

    public void deleteLicense(License license){
        licenseRepository.delete( license.getLicenseId());
    }

    private void cacheOrganization(Organization org) {
        try {
            orgRedisRepo.saveOrganization(org);
        } catch (Exception e) {
            logger.error("Unable to cache organization {} in Redis. Exception: {}", org.getId(), e);
        }
    }

    private List<License> buildFallbackLicenseList(String organizationId){
        List<License> fallbackList = new ArrayList<>();
        License license = new License()
                .withId("0000000-00-00000")
                .withOrganizationId( organizationId )
                .withProductName("Sorry no licensing information currently available");

        fallbackList.add(license);
        return fallbackList;
    }

    /**
     * Tests with timeout / Hystrix
     */
    private void randomlyRunLong(){
        Random rand = new Random();
        int randomNum = rand.nextInt((3 - 1) + 1) + 1;
        if (randomNum==3) sleep();
    }

    private void sleep(){
        try { Thread.sleep(11000); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }

}
