package com.valura.auth.scim.service;

import com.unboundid.scim2.common.exceptions.ResourceNotFoundException;
import com.unboundid.scim2.common.exceptions.BadRequestException;
import com.unboundid.scim2.common.exceptions.PreconditionFailedException;
import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.types.Email;
import com.unboundid.scim2.common.types.Meta;
import com.unboundid.scim2.common.types.Name; // Ensure this is imported
import com.unboundid.scim2.common.types.UserResource;
import com.unboundid.scim2.server.annotations.ResourceType;
import com.valura.auth.database.entity.UserEntity;
import com.valura.auth.database.repository.UserRepository;
import com.valura.auth.scim.model.ScimListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;

@Service
@ResourceType(description = "User Account", name = "User", schema = UserResource.class)
public class ScimUserService {
    private final UserRepository userRepository;

    public ScimUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResource create(UserResource user) throws BadRequestException {
        if (userRepository.existsByUserName(user.getUserName())) {
            throw BadRequestException.uniqueness("Username already exists");
        }

        UserEntity entity = new UserEntity();
        entity.setExternalId(UUID.randomUUID().toString()); // ExternalId set here for new users
        updateEntityFromScim(entity, user); // This will now map name fields
        entity = userRepository.save(entity);
        return mapToScimUser(entity);
    }

    public UserResource get(String id) throws ResourceNotFoundException {
        UserEntity entity = userRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToScimUser(entity);
    }

    @Transactional
    public UserResource replace(String id, UserResource user, String ifMatch) throws ScimException {
        UserEntity entity = userRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserResource existingUser = mapToScimUser(entity);
        validateETag(existingUser.getMeta().getVersion(), ifMatch);

        // Do NOT set ExternalId here, as it's an existing user.
        // If the SCIM client sends an 'id' in the PUT body, it implicitly means the existing resource.
        updateEntityFromScim(entity, user); // This will now map name fields
        entity = userRepository.save(entity);
        return mapToScimUser(entity);
    }

    @Transactional
    public UserResource patch(String id, UserResource user, String ifMatch) throws ScimException {
        UserEntity entity = userRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserResource existingUser = mapToScimUser(entity);
        validateETag(existingUser.getMeta().getVersion(), ifMatch);

        // Apply partial updates from 'user' to 'entity'
        // This is a simplified example; a real SCIM PATCH would be more complex
        // and involve operations like add, remove, replace.
        // It should also call a more sophisticated update method or apply operations directly.
        if (user.getUserName() != null) {
            entity.setUserName(user.getUserName());
        }
        if (user.getDisplayName() != null) {
            entity.setDisplayName(user.getDisplayName());
        }
        if (user.getActive() != null) {
            entity.setActive(user.getActive());
        }
        if (user.getEmails() != null && !user.getEmails().isEmpty()) {
            entity.setEmail(user.getEmails().get(0).getValue());
        }
        // For PATCH, you'd typically need to check for name presence as well
        if (user.getName() != null) {
            if (user.getName().getGivenName() != null) {
                entity.setFirstName(user.getName().getGivenName());
            }
            if (user.getName().getFamilyName() != null) {
                entity.setLastName(user.getName().getFamilyName());
            }
        }

        entity = userRepository.save(entity);
        return mapToScimUser(entity);
    }

    @Transactional
    public void delete(String id) throws ResourceNotFoundException {
        UserEntity entity = userRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(entity);
    }

    public ScimListResponse<UserResource> search(Integer startIndex, Integer count, String filter) {
        try {
            int pageNumber = (startIndex != null && startIndex > 0) ? startIndex - 1 : 0;
            int pageSize = (count != null && count > 0) ? count : 100;

            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            Page<UserEntity> page;

            if (filter != null && !filter.trim().isEmpty()) {
                System.out.println("Processing filter: " + filter);

                FilterResult filterResult = parseFilter(filter);

                if (filterResult != null) {
                    switch (filterResult.getAttribute().toLowerCase()) {
                        case "username":
                            page = userRepository.findByUserNameContainingIgnoreCase(
                                    filterResult.getValue(), pageable);
                            break;
                        case "displayname":
                            page = userRepository.findByDisplayNameContainingIgnoreCase(
                                    filterResult.getValue(), pageable);
                            break;
                        case "emails":
                        case "emails.value":
                            page = userRepository.findByEmailContainingIgnoreCase(
                                    filterResult.getValue(), pageable);
                            break;
                        case "active":
                            boolean activeValue = Boolean.parseBoolean(filterResult.getValue());
                            page = userRepository.findByActive(activeValue, pageable);
                            break;
                        case "name.givenname": // Added filter for givenName
                            page = userRepository.findByFirstNameContainingIgnoreCase(
                                    filterResult.getValue(), pageable);
                            break;
                        case "name.familyname": // Added filter for familyName
                            page = userRepository.findByLastNameContainingIgnoreCase(
                                    filterResult.getValue(), pageable);
                            break;
                        default:
                            System.out.println("Unsupported filter attribute: " + filterResult.getAttribute() + ", returning all users");
                            page = userRepository.findAll(pageable);
                    }
                } else {
                    System.out.println("Could not parse filter, returning all users");
                    page = userRepository.findAll(pageable);
                }
            } else {
                page = userRepository.findAll(pageable);
            }

            List<UserResource> resources = page.getContent().stream()
                    .map(this::mapToScimUser)
                    .collect(Collectors.toList());

            return ScimListResponse.<UserResource>builder()
                    .resources(resources)
                    .totalResults((int) page.getTotalElements())
                    .startIndex(startIndex != null ? startIndex : 1)
                    .itemsPerPage(resources.size()) // Use resources.size() for actual items per page
                    .build();

        } catch (Exception e) {
            System.err.println("Error in search method: " + e.getMessage());
            e.printStackTrace();

            return ScimListResponse.<UserResource>builder()
                    .resources(new ArrayList<>())
                    .totalResults(0)
                    .startIndex(startIndex != null ? startIndex : 1)
                    .itemsPerPage(count != null ? count : 100)
                    .build();
        }
    }

    private FilterResult parseFilter(String filter) {
        try {
            filter = filter.trim();
            Pattern pattern = Pattern.compile("(\\w+(?:\\.\\w+)?)\\s+(eq|ne|co|sw|ew|gt|lt|ge|le)\\s+[\"']?([^\"']+)[\"']?", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(filter);

            if (matcher.find()) {
                String attribute = matcher.group(1);
                String operator = matcher.group(2);
                String value = matcher.group(3);

                System.out.println("Parsed filter - Attribute: " + attribute + ", Operator: " + operator + ", Value: " + value);

                return new FilterResult(attribute, operator, value);
            }
        } catch (Exception e) {
            System.err.println("Error parsing filter: " + e.getMessage());
        }

        return null;
    }

    private void updateEntityFromScim(UserEntity entity, UserResource user) {
        entity.setUserName(user.getUserName());
        entity.setDisplayName(user.getDisplayName());
        entity.setActive(user.getActive());

        if (user.getEmails() != null && !user.getEmails().isEmpty()) {
            entity.setEmail(user.getEmails().get(0).getValue());
        }

        // --- NEW/UPDATED MAPPING FOR NAME ---
        if (user.getName() != null) {
            // Only update if provided in the SCIM UserResource
            if (user.getName().getGivenName() != null) {
                entity.setFirstName(user.getName().getGivenName());
            }
            if (user.getName().getFamilyName() != null) {
                entity.setLastName(user.getName().getFamilyName());
            }
        }
        // --- END NEW/UPDATED MAPPING ---
    }

    private UserResource mapToScimUser(UserEntity entity) {
        UserResource scimUser = new UserResource();
        scimUser.setId(entity.getExternalId());
        scimUser.setUserName(entity.getUserName());
        scimUser.setDisplayName(entity.getDisplayName());
        scimUser.setActive(entity.isActive());

        // --- NEW/UPDATED MAPPING FOR NAME ---
        if (entity.getFirstName() != null || entity.getLastName() != null) {
            Name name = new Name(); // Use UnboundID's Name class
            name.setGivenName(entity.getFirstName());
            name.setFamilyName(entity.getLastName());
            if (entity.getFirstName() != null && entity.getLastName() != null) {
                name.setFormatted(entity.getFirstName() + " " + entity.getLastName());
            } else if (entity.getFirstName() != null) {
                name.setFormatted(entity.getFirstName());
            } else if (entity.getLastName() != null) {
                name.setFormatted(entity.getLastName());
            }
            scimUser.setName(name); // Set the populated Name object
        }
        // --- END NEW/UPDATED MAPPING ---

        if (entity.getEmail() != null) {
            Email email = new Email();
            email.setValue(entity.getEmail());
            email.setPrimary(true);
            email.setType("work");
            List<Email> emails = new ArrayList<>();
            emails.add(email);
            scimUser.setEmails(emails);
        }

        Meta meta = new Meta();
        meta.setResourceType("User");

        if (entity.getCreatedAt() != null) {
            Calendar created = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            created.setTimeInMillis(entity.getCreatedAt().toEpochMilli());
            meta.setCreated(created);
        }

        if (entity.getUpdatedAt() != null) {
            Calendar modified = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            modified.setTimeInMillis(entity.getUpdatedAt().toEpochMilli());
            meta.setLastModified(modified);
        }

        try {
            meta.setLocation(URI.create("/scim/v2/Users/" + entity.getExternalId()));
            meta.setVersion(generateETag(scimUser));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid URI format", e);
        }

        scimUser.setMeta(meta);

        return scimUser;
    }

    private String generateETag(UserResource userResource) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            String data = userResource.getId() + userResource.getUserName() + userResource.getDisplayName() +
                    userResource.getActive() +
                    (userResource.getMeta() != null && userResource.getMeta().getLastModified() != null ?
                            userResource.getMeta().getLastModified().getTimeInMillis() : "");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            return "W/\"" + DatatypeConverter.printHexBinary(hash).toLowerCase() + "\"";
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found for ETag generation", e);
        }
    }

    private void validateETag(String currentETag, String ifMatchHeader) throws PreconditionFailedException {
        if (ifMatchHeader != null && !ifMatchHeader.isEmpty()) {
            if (!currentETag.equals(ifMatchHeader)) {
                throw new PreconditionFailedException("ETag mismatch");
            }
        }
    }

    private static class FilterResult {
        private final String attribute;
        private final String operator;
        private final String value;

        public FilterResult(String attribute, String operator, String value) {
            this.attribute = attribute;
            this.operator = operator;
            this.value = value;
        }

        public String getAttribute() {
            return attribute;
        }

        public String getOperator() {
            return operator;
        }

        public String getValue() {
            return value;
        }
    }
}