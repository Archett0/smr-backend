package com.team12.searchservice.repository;

import com.team12.searchservice.document.UserDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, String> {

    /**
     * Find users by role
     */
    List<UserDocument> findByRole(String role);

    /**
     * Find active users by role
     */
    List<UserDocument> findByRoleAndActiveTrue(String role);

    /**
     * Search users by name
     */
    @Query("{\"bool\": {\"must\": [{\"match\": {\"name\": \"?0\"}}]}}")
    Page<UserDocument> findByNameContaining(String name, Pageable pageable);

    /**
     * Search agents by location and specialties
     */
    @Query("{\"bool\": {\"must\": [{\"match\": {\"role\": \"AGENT\"}}, {\"match\": {\"location\": \"?0\"}}], \"should\": [{\"terms\": {\"specialties\": [?1]}}]}}")
    Page<UserDocument> findAgentsByLocationAndSpecialties(String location, String[] specialties, Pageable pageable);

    /**
     * Find top rated agents
     */
    @Query("{\"bool\": {\"must\": [{\"match\": {\"role\": \"AGENT\"}}, {\"range\": {\"rating\": {\"gte\": ?0}}}]}}")
    List<UserDocument> findTopRatedAgents(Integer minRating);

    /**
     * Find users by email
     */
    UserDocument findByEmail(String email);

    /**
     * Find users by userId
     */
    UserDocument findByUserId(String userId);
}