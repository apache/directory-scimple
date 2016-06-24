package edu.psu.swe.scim.rdbms.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.rdbms.model.Address;
import edu.psu.swe.scim.rdbms.model.Address_;
import edu.psu.swe.scim.rdbms.model.Person;
import edu.psu.swe.scim.rdbms.model.Person_;
import edu.psu.swe.scim.rdbms.model.Phone;
import edu.psu.swe.scim.rdbms.model.Phone_;
import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveExtensionsException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.protocol.filter.AttributeComparisonExpression;
import edu.psu.swe.scim.spec.protocol.filter.FilterExpression;
import edu.psu.swe.scim.spec.protocol.filter.FilterResponse;
import edu.psu.swe.scim.spec.protocol.filter.LogicalExpression;
import edu.psu.swe.scim.spec.protocol.filter.LogicalOperator;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.Name;
import edu.psu.swe.scim.spec.resources.PhoneNumber;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Stateless
@SuppressWarnings("rawtypes")
public class ScimRdbmsService implements Provider<ScimUser> {

  @PersistenceContext(name="ExampleDS")
  EntityManager entityManager;
  
  //static Map<String, SingularAttribute<?,?>> tableAliasMap = new HashMap<>();
  static Map<String, AttributeType> tableAliasMap = new HashMap<>();
  
  @AllArgsConstructor
  static class AttributeType<T> {
     SingularAttribute<?, T> attribute;
     Class<T> clazz;
  }
    
  static {
    
    AttributeType at = new AttributeType<String>(Address_.streetAddress, String.class);
    tableAliasMap.put("addresses.streetAddress", at);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "addresses.streetAddress", at);
    
    at = new AttributeType<String>(Address_.city, String.class);
    tableAliasMap.put("addresses.locality", at);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "addresses.locality", at);
    
    at = new AttributeType<String>(Address_.state, String.class);
    tableAliasMap.put("addresses.region", at);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "addresses.region", at);
    
    at = new AttributeType<String>(Address_.zipCode, String.class);
    tableAliasMap.put("addresses.postalCode", at);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "addresses.postalCode", at);
    
    at = new AttributeType<String>(Address_.countryCode, String.class);
    tableAliasMap.put("addresses.country", at);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "addresses.country", at);
    
    at = new AttributeType<String>(Phone_.type, String.class);
    tableAliasMap.put("phoneNumbers.type", at);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "phoneNumbers.type", at);
    
    at = new AttributeType<String>(Phone_.number, String.class);
    tableAliasMap.put("phoneNumbers.value", at);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "phoneNumbers.value", at);
    
    at = new AttributeType<String>(Person_.lastName, String.class);
    tableAliasMap.put("name.familyName", at);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "name.familyName", at);
    
    at = new AttributeType<String>(Person_.firstName, String.class);
    tableAliasMap.put("name.givenName", at);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "name.givenName", at);
    
    at = new AttributeType<String>(Person_.middleName, String.class);
    tableAliasMap.put("name.middleName", at);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "name.middleName", at);
    
    at = new AttributeType<Boolean>(Person_.active, Boolean.class);
    tableAliasMap.put("active", at);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "active", at);
    
    at = new AttributeType<Long>(Person_.personId, long.class);
    tableAliasMap.put("person_id", at);    
  }
  
  CriteriaBuilder criteriaBuilder = null;
  CriteriaQuery<Person> criteriaQuery = null;
  Root<Person> queryRoot = null;
  ListJoin<Person, Address> addressJoin = null;
  ListJoin<Person, Phone> phoneJoin = null;
  
  Map<String, Join> joinMap = null;
  
  @Override
  public ScimUser create(ScimUser resource) throws UnableToCreateResourceException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ScimUser update(String id, ScimUser resource) throws UnableToUpdateResourceException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ScimUser get(String id) throws UnableToRetrieveResourceException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FilterResponse<ScimUser> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws UnableToRetrieveResourceException {
    log.info("@@@@@@@ In find");

    FilterExpression expression = filter.getExpression();

    log.info("Processing filter " + filter.getFilter());
    
    criteriaQuery.select(queryRoot);
    
    Predicate predicate = processExpression(expression, criteriaBuilder);
    criteriaQuery.where(predicate).distinct(true);
    
    TypedQuery<Person> tq = entityManager.createQuery(criteriaQuery);
    List<Person> personList = tq.getResultList();
    log.info("---> Query executed, " + personList.size() + " returns");
    
    List<ScimUser> scimUserList = new ArrayList<>();
    
    for (Person p : personList) {
      ScimUser su = new ScimUser();
      List<edu.psu.swe.scim.spec.resources.Address> scimAddressList = new ArrayList<>();
      
      for (Address a : p.getAddressList()) {
        edu.psu.swe.scim.spec.resources.Address sa = new edu.psu.swe.scim.spec.resources.Address();
        sa.setPostalCode(a.getZipCode());
        sa.setStreetAddress(a.getStreetAddress());
        sa.setLocality(a.getCity());
        sa.setRegion(a.getState());
        sa.setCountry(a.getCountryCode());
        scimAddressList.add(sa);
      }
      
      su.setAddresses(scimAddressList);
      
      List<edu.psu.swe.scim.spec.resources.PhoneNumber> scimPhoneList = new ArrayList<>();
      for (Phone phone : p.getPhoneList()) {
        PhoneNumber pn = new PhoneNumber();
        pn.setValue(phone.getNumber());
        scimPhoneList.add(pn);
      }
      
      su.setPhoneNumbers(scimPhoneList);
      
      Name name = new Name();
      name.setFamilyName(p.getLastName());
      name.setGivenName(p.getFirstName());
      name.setMiddleName(p.getMiddleName());
      su.setName(name);
      scimUserList.add(su);
    }
    
    FilterResponse<ScimUser> response = new FilterResponse<>();
    response.setResources(scimUserList);
    PageRequest pr = new PageRequest();
    pr.setCount(scimUserList.size());
    pr.setStartIndex(0);
    response.setPageRequest(pr);
    response.setTotalResults(scimUserList.size());
    
    return response;
  }

  private Predicate processExpression(FilterExpression expression, CriteriaBuilder criteriaBuilder) throws UnableToRetrieveResourceException {
    
    if (expression instanceof LogicalExpression) {
      LogicalExpression le = (LogicalExpression) expression;
      Predicate p1 = processExpression(le.getLeft(), criteriaBuilder);
      Predicate p2 = processExpression(le.getRight(), criteriaBuilder);
      
      if (le.getOperator().equals(LogicalOperator.AND)) {
        return criteriaBuilder.and(p1, p2);
      } else if (le.getOperator().equals(LogicalOperator.OR)) {
        return criteriaBuilder.or(p1, p2);
      }
    } else if (expression instanceof AttributeComparisonExpression) {
      
      AttributeComparisonExpression ace = (AttributeComparisonExpression) expression;
      
      String attributeBase = ace.getAttributePath().getAttributeBase();
      
      Join join = joinMap.get(attributeBase);
      Path path = null;
      
      AttributeType attributeType = tableAliasMap.get(ace.getAttributePath().getFullAttributeName());
      
      if (attributeType == null) {
        throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "Unable to map filter attribute " + ace.getAttributePath().getFullAttributeName());
      }
      
      if (join == null) {
        AttributeReference attributePath = ace.getAttributePath();
        path = queryRoot.get((SingularAttribute<? super Person, ?>) attributeType.attribute);
      } else {
        path = join.get(attributeType.attribute);
      }
      
      if (path == null) {
        throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "Unable to map filter attribute " + ace.getAttributePath().getFullAttributeName());
      }
      
      switch(ace.getOperation()) {
        case EQ:
          return criteriaBuilder.equal(path, ace.getCompareValue());
        case CO:
          return criteriaBuilder.like(path, "%" + ace.getCompareValue() + "%");
        case EW:
          return criteriaBuilder.like(path, "%" + ace.getCompareValue());
        case GE:
          if (attributeType.clazz.equals(Long.class) || attributeType.clazz.equals(long.class)) {
            Long l = Long.parseLong((String)ace.getCompareValue());
            return criteriaBuilder.greaterThanOrEqualTo(path, l);
          } else if (attributeType.clazz.equals(Integer.class) || attributeType.clazz.equals(int.class)) {
            Integer i = Integer.parseInt(((String)ace.getCompareValue()));
            return criteriaBuilder.greaterThanOrEqualTo(path, i);
          } else if (attributeType.clazz.equals(Float.class) || attributeType.clazz.equals(float.class)) {
            Float f = Float.parseFloat(((String)ace.getCompareValue()));
            return criteriaBuilder.greaterThanOrEqualTo(path, f);
          } else if (attributeType.clazz.equals(Double.class) || attributeType.clazz.equals(double.class)) {
            Double d = Double.parseDouble(((String)ace.getCompareValue()));
            return criteriaBuilder.greaterThanOrEqualTo(path, d);
          } else {
            throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "Invalid value type for boolean evaluation (GT), " + ace.getAttributePath().getFullAttributeName());
          }
        case GT:
          if (attributeType.clazz.equals(Long.class) || attributeType.clazz.equals(long.class)) {
            Long l = Long.parseLong((String)ace.getCompareValue());
            return criteriaBuilder.greaterThan(path, l);
          } else if (attributeType.clazz.equals(Integer.class) || attributeType.clazz.equals(int.class)) {
            Integer i = Integer.parseInt(((String)ace.getCompareValue()));
            return criteriaBuilder.greaterThan(path, i);
          } else if (attributeType.clazz.equals(Float.class) || attributeType.clazz.equals(float.class)) {
            Float f = Float.parseFloat(((String)ace.getCompareValue()));
            return criteriaBuilder.greaterThan(path, f);
          } else if (attributeType.clazz.equals(Double.class) || attributeType.clazz.equals(double.class)) {
            Double d = Double.parseDouble(((String)ace.getCompareValue()));
            return criteriaBuilder.greaterThan(path, d);
          } else {
            throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "Invalid value type for boolean evaluation (GT), " + ace.getAttributePath().getFullAttributeName());
          }
//          else if (attributeType.clazz.equals(Date.class)) {
//            Date d = Long.parseLong(((String)ace.getCompareValue());
//            return criteriaBuilder.greaterThan(path, d);
//          }
        case LE:
          if (attributeType.clazz.equals(Long.class) || attributeType.clazz.equals(long.class)) {
            Long l = Long.parseLong((String)ace.getCompareValue());
            return criteriaBuilder.lessThanOrEqualTo(path, l);
          } else if (attributeType.clazz.equals(Integer.class) || attributeType.clazz.equals(int.class)) {
            Integer i = Integer.parseInt(((String)ace.getCompareValue()));
            return criteriaBuilder.lessThanOrEqualTo(path, i);
          } else if (attributeType.clazz.equals(Float.class) || attributeType.clazz.equals(float.class)) {
            Float f = Float.parseFloat(((String)ace.getCompareValue()));
            return criteriaBuilder.lessThanOrEqualTo(path, f);
          } else if (attributeType.clazz.equals(Double.class) || attributeType.clazz.equals(double.class)) {
            Double d = Double.parseDouble(((String)ace.getCompareValue()));
            return criteriaBuilder.lessThanOrEqualTo(path, d);
          } else {
            throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "Invalid value type for boolean evaluation (GT), " + ace.getAttributePath().getFullAttributeName());
          }
        case LT:
          if (attributeType.clazz.equals(Long.class) || attributeType.clazz.equals(long.class)) {
            Long l = Long.parseLong((String)ace.getCompareValue());
            return criteriaBuilder.lessThan(path, l);
          } else if (attributeType.clazz.equals(Integer.class) || attributeType.clazz.equals(int.class)) {
            Integer i = Integer.parseInt(((String)ace.getCompareValue()));
            return criteriaBuilder.lessThan(path, i);
          } else if (attributeType.clazz.equals(Float.class) || attributeType.clazz.equals(float.class)) {
            Float f = Float.parseFloat(((String)ace.getCompareValue()));
            return criteriaBuilder.lessThan(path, f);
          } else if (attributeType.clazz.equals(Double.class) || attributeType.clazz.equals(double.class)) {
            Double d = Double.parseDouble(((String)ace.getCompareValue()));
            return criteriaBuilder.lessThan(path, d);
          } else {
            throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "Invalid value type for boolean evaluation (GT), " + ace.getAttributePath().getFullAttributeName());
          }
        case NE:
          //return criteriaBuilder.notEqual(x, y);
          break;
        case SW:
          return criteriaBuilder.like(path, ace.getCompareValue() + "%");
        default:
          break;
      }
    }
        
    return null;
    //log.info("------> sql = " + sb.toString());//  + '\n' + "--------> query.toString() = " + query.toString());
  }
  
  @Override
  public void delete(String id) throws UnableToDeleteResourceException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() throws UnableToRetrieveExtensionsException {
    // TODO Auto-generated method stub
    return null;
  }


  private boolean processExpressionElement(FilterExpression expression, CriteriaBuilder builder) {
     return false;
  }
  
  @PostConstruct
  private void initialize() {
    criteriaBuilder = entityManager.getCriteriaBuilder();
    criteriaQuery = criteriaBuilder.createQuery(Person.class);
    queryRoot = criteriaQuery.from(Person.class);
    addressJoin = queryRoot.join(Person_.addressList, JoinType.LEFT);
    phoneJoin = queryRoot.join(Person_.phoneList, JoinType.LEFT);
    
    joinMap = new HashMap<>();
    
    joinMap.put("phones", phoneJoin);
    joinMap.put("addresses", addressJoin);
  }
}
