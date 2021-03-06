package com.intrasso;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intrasso.model.*;
import com.intrasso.repository.AssociationRepository;
import com.intrasso.repository.PageWithFormRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Util {
    public static Queue<PageWithForm> getObjects(AssociationRepository repository, String type, List<Association> associationList, long id, long userId) {
        Queue<PageWithForm> objectQueue;
        if (type.equals("events")) {
            objectQueue = new PriorityQueue<>(Comparator.comparing(PageWithForm::getEndDate));
        } else {
            objectQueue = new PriorityQueue<>(Comparator.comparing(PageWithForm::getCreatedAt));
        }
        if (id != -1) {
            associationList.add(repository.getOne(id));
        } else if (associationList == null) {
            associationList = repository.findAll();
        }
        for (Association association : associationList) {
            boolean isMember = getMember(association, userId) != null;
            objectQueue.addAll(association.getByType(type, isMember));
        }
        return objectQueue;
    }

    public static Queue<PageWithForm> getObjects(AssociationRepository repository, String type, List<Association> associationList, long userId) {
        return Util.getObjects(repository, type, associationList, -1, userId);
    }

    public static Queue<PageWithForm> getObjects(AssociationRepository repository, String type, long id, long userId) {
        return Util.getObjects(repository, type, new ArrayList<>(), id, userId);
    }

    public static Queue<PageWithForm> getObjects(AssociationRepository repository, String type, long userId) {
        return Util.getObjects(repository, type, null, -1, userId);
    }

    public static List<PageWithForm> getSome(Queue<PageWithForm> queue, int size, PageWithFormRepository repository) {
        List<PageWithForm> newList = new ArrayList<>();
        while (size > 0 && queue.size() > 0) {
            PageWithForm element = queue.remove();
            if (element.getType().equals("event")) {
                if (element.getEndDate().before(new Date())) {
                    repository.deleteById(element.getId());
                    element = null;
                }
            }
            if (element != null) {
                size--;
                newList.add(element);
            }
        }
        return newList;
    }

    public static Member getMember(Association association, long userId){
        for(Member member : association.getMembers()){
            if(member.getUser().getId() == userId){
                return member;
            }
        }
        return null;
    }

    public static Map<String, Long> getMapMember(List<Member> memberList) {
        Map<String, Long> memberMap = new HashMap<>();
        for (Member member : memberList) {
            memberMap.put(member.getAssociation().getName(), member.getAssociation().getId());
        }
        return memberMap;
    }

    public static String objectToString(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            System.out.println("error : " + e.getMessage());
        }
        return "";
    }

    public static Object stringToObject(String json, TypeReference typeReference) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            System.out.println("error : " + e.getMessage());
        }
        return null;
    }

    public static Candidate getCandidate(PageWithForm pageWithForm, HttpServletRequest request){
        for(Candidate candidate : pageWithForm.getCandidateList()){
            if(candidate.getUser().getId().equals(request.getSession().getAttribute("userId"))){
                return candidate;
            }
        }
        return null;
    }
}


