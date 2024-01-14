package com.ethanaa.crudstar.repository.specification;

import com.ethanaa.crudstar.model.api.ApiFilter;
import com.ethanaa.crudstar.model.api.FilterConstraint;
import com.ethanaa.crudstar.model.persist.patient.Patient;
import com.ethanaa.crudstar.model.persist.patient.patch.PatientPatchEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PatientPatchEntitySpecification implements Specification<PatientPatchEntity> {

    private String searchQuery;
    private List<ApiFilter> filters;
    private Pageable pageable;

    public PatientPatchEntitySpecification(String searchQuery, List<ApiFilter> filters, Pageable pageable) {

        this.searchQuery = searchQuery;
        this.filters = filters;
        this.pageable = pageable;
    }

    @Override
    public Predicate toPredicate(Root<PatientPatchEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        List<Predicate> predicates = new ArrayList<>();

        // TODO convert findPatchesAsOfDateTime query
        Subquery<PatientPatchEntity> subquery = query.subquery(PatientPatchEntity.class);

        if (StringUtils.hasText(searchQuery)) {
            predicates.add(builder.equal(
                    builder.function("json_search_function", Boolean.class,
                            root.get("patch"), builder.literal(searchQuery)),
                    true));
        }

        for (ApiFilter filter : filters) {
            if (!StringUtils.hasText(filter.getKey())) {
                continue;
            }
            if (!StringUtils.hasText(filter.getOperator())) {
                continue;
            }
            List<Predicate> constraintPredicates = new ArrayList<>();
            for (FilterConstraint constraint : filter.getConstraints()) {
                switch(constraint.getMatchMode()) {
                    case "startsWith":
                        constraintPredicates.add(builder.like(builder.lower(
                                builder.function("jsonb_extract_path_text",
                                        String.class,
                                        root.<Patient>get("patient"),
                                        builder.literal(filter.getKey()))),
                                constraint.getValue().toLowerCase() + "%"));
                        break;
                    case "endsWith":
                        constraintPredicates.add(builder.like(builder.lower(
                                builder.function("jsonb_extract_path_text",
                                        String.class,
                                        root.<Patient>get("patient"),
                                        builder.literal(filter.getKey()))),
                                "%" + constraint.getValue().toLowerCase()));
                        break;
                    case "contains":
                        constraintPredicates.add(builder.like(builder.lower(
                                builder.function("jsonb_extract_path_text",
                                        String.class,
                                        root.<Patient>get("patient"),
                                        builder.literal(filter.getKey()))),
                                "%" + constraint.getValue().toLowerCase() + "%"));
                        break;
                    case "equals":
                        constraintPredicates.add(builder.equal(builder.lower(
                                builder.function("jsonb_extract_path_text",
                                        String.class,
                                        root.<Patient>get("patient"),
                                        builder.literal(filter.getKey()))), constraint.getValue().toLowerCase()));
                        break;
                    case "dateIs":
                        LocalDateTime isValue = LocalDateTime.parse(constraint.getValue(), DateTimeFormatter.ISO_DATE_TIME);
                        constraintPredicates.add(builder.equal(
                                builder.function("date", LocalDateTime.class,
                                        builder.function("jsonb_extract_path_text",
                                                String.class,
                                                root.<Patient>get("patient"),
                                                builder.literal(filter.getKey()))), isValue));
                        break;
                    case "dateIsNot":
                        LocalDateTime isNotValue = LocalDateTime.parse(constraint.getValue(), DateTimeFormatter.ISO_DATE_TIME);
                        constraintPredicates.add(builder.notEqual(
                                builder.function("date", LocalDateTime.class,
                                        builder.function("jsonb_extract_path_text",
                                                String.class,
                                                root.<Patient>get("patient"),
                                                builder.literal(filter.getKey()))), isNotValue));
                        break;
                    case "dateBefore":
                        LocalDateTime beforeValue = LocalDateTime.parse(constraint.getValue(), DateTimeFormatter.ISO_DATE_TIME);
                        constraintPredicates.add(builder.lessThan(
                                builder.function("date", LocalDateTime.class,
                                        builder.function("jsonb_extract_path_text",
                                                String.class,
                                                root.<Patient>get("patient"),
                                                builder.literal(filter.getKey()))), beforeValue));
                        break;
                    case "dateAfter":
                        LocalDateTime afterValue = LocalDateTime.parse(constraint.getValue(), DateTimeFormatter.ISO_DATE_TIME);
                        constraintPredicates.add(builder.greaterThan(
                                builder.function("date", LocalDateTime.class,
                                        builder.function("jsonb_extract_path_text",
                                                String.class,
                                                root.<Patient>get("patient"),
                                                builder.literal(filter.getKey()))), afterValue));
                        break;
                    default:
                }
            }

            if (filter.getOperator().equalsIgnoreCase("or")) {
                predicates.add(builder.or(constraintPredicates.toArray(new Predicate[0])));
            } else {
                predicates.add(builder.and(constraintPredicates.toArray(new Predicate[0])));
            }
        }

        List<Order> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            Expression sortExpression = builder.function("jsonb_extract_path_text",
                    String.class,
                    root.<Patient>get("patient"),
                    builder.literal(order.getProperty()));
            switch (order.getDirection()) {
                case ASC:
                    orders.add(builder.asc(sortExpression));
                    break;
                case DESC:
                    orders.add(builder.desc(sortExpression));
                    break;
            }
        }
        orders.add(builder.desc(root.get("updatedAt")));

        query.orderBy(orders);

        if (!predicates.isEmpty()) {
            return builder.and(predicates.toArray(new Predicate[0]));
        }

        return null;
    }
}
