package com.securevault.backend.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securevault.backend.entity.Team;
import com.securevault.backend.entity.TeamMember;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.TeamMemberRepository;
import com.securevault.backend.repository.TeamRepository;
import com.securevault.backend.repository.UserRepository;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamService(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            UserRepository userRepository
    ) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    public Team createTeam(String ownerEmail, String teamName) {

        User owner = getUserByEmail(ownerEmail);

        if (teamName == null || teamName.trim().isEmpty()) {
            throw new IllegalArgumentException("Team name is required");
        }

        String name = teamName.trim();

        if (teamRepository.existsByNameAndOwnerId(name, owner.getId())) {
            throw new IllegalArgumentException(
                    "You already have a team with this name"
            );
        }

        Team team = new Team();
        team.setName(name);
        team.setOwner(owner);

        Team savedTeam = teamRepository.save(team);

        // Owner automatically becomes a team member.
        TeamMember ownerMember = new TeamMember();
        ownerMember.setTeam(savedTeam);
        ownerMember.setUser(owner);
        ownerMember.setRole("OWNER");

        teamMemberRepository.save(ownerMember);

        return savedTeam;
    }

    public List<Team> getMyTeams(String ownerEmail) {

        User owner = getUserByEmail(ownerEmail);

        return teamRepository.findByOwnerId(owner.getId());
    }

    public Team getTeamForOwner(Long teamId, String ownerEmail) {

        User owner = getUserByEmail(ownerEmail);

        return teamRepository.findByIdAndOwnerId(
                teamId,
                owner.getId()
        ).orElseThrow(() ->
                new AccessDeniedException(
                        "You do not have permission to access this team"
                )
        );
    }

    public void addMember(
            Long teamId,
            String ownerEmail,
            String memberEmail,
            String role
    ) {

        User owner = getUserByEmail(ownerEmail);

        Team team = teamRepository.findByIdAndOwnerId(
                teamId,
                owner.getId()
        ).orElseThrow(() ->
                new AccessDeniedException(
                        "You do not have permission to manage this team"
                )
        );

        if (memberEmail == null || memberEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Member email is required");
        }

        User member = getUserByEmail(memberEmail.trim());

        if (teamMemberRepository.existsByTeamIdAndUserId(
                team.getId(),
                member.getId()
        )) {
            throw new IllegalArgumentException(
                    "User is already a member of this team"
            );
        }

        if (member.getId().equals(owner.getId())) {
            throw new IllegalArgumentException(
                    "Team owner is already a member"
            );
        }

        String normalizedRole = normalizeRole(role);

        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setUser(member);
        teamMember.setRole(normalizedRole);

        teamMemberRepository.save(teamMember);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> getTeamMembers(
            Long teamId,
            String ownerEmail
    ) {

        User owner = getUserByEmail(ownerEmail);

        teamRepository.findByIdAndOwnerId(
                teamId,
                owner.getId()
        ).orElseThrow(() ->
                new AccessDeniedException(
                        "You do not have permission to access this team"
                )
        );

        return teamMemberRepository.findByTeamId(teamId);
    }

    public void removeMember(
            Long teamId,
            String ownerEmail,
            Long memberUserId
    ) {

        User owner = getUserByEmail(ownerEmail);

        Team team = teamRepository.findByIdAndOwnerId(
                teamId,
                owner.getId()
        ).orElseThrow(() ->
                new AccessDeniedException(
                        "You do not have permission to manage this team"
                )
        );

        if (owner.getId().equals(memberUserId)) {
            throw new IllegalArgumentException(
                    "Team owner cannot be removed"
            );
        }

        TeamMember member = teamMemberRepository
                .findByTeamIdAndUserId(team.getId(), memberUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User is not a member of this team"
                        )
                );

        teamMemberRepository.delete(member);
    }

    private String normalizeRole(String role) {

        if (role == null || role.trim().isEmpty()) {
            return "MEMBER";
        }

        String normalized = role.trim().toUpperCase();

        if (!normalized.equals("MEMBER")
                && !normalized.equals("ADMIN")) {

            throw new IllegalArgumentException(
                    "Invalid team role. Use MEMBER or ADMIN"
            );
        }

        return normalized;
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }
}