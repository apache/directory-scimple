package edu.psu.swe.scim.rdbms.service;

import java.util.ArrayList;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Stateless
@SuppressWarnings("rawtypes")
public class ScimRdbmsService implements Provider<ScimUser> {

  @PersistenceContext(name="ExampleDS")
  EntityManager entityManager;
  
  static Map<String, SingularAttribute<?,?>> tableAliasMap = new HashMap<>();
    
  static {
    tableAliasMap.put("addresses.streetAddress", Address_.streetAddress);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "addresses.streetAddress", Address_.streetAddress);
    tableAliasMap.put("addresses.locality", Address_.city);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "addresses.locality", Address_.city);
    tableAliasMap.put("addresses.region", Address_.state);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "addresses.region", Address_.state);
    tableAliasMap.put("addresses.postalCode", Address_.zipCode);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "addresses.postalCode", Address_.zipCode);
    tableAliasMap.put("addresses.country", Address_.countryCode);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "addresses.country", Address_.countryCode);
    
    tableAliasMap.put("phoneNumbers.type", Phone_.type);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "phoneNumbers.type", Phone_.type);
    tableAliasMap.put("phoneNumbers.value", Phone_.number);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "phoneNumbers.value", Phone_.number);
    
    tableAliasMap.put("name.familyName", Person_.lastName);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "name.familyName", Person_.lastName);
    tableAliasMap.put("name.givenName", Person_.firstName);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "name.givenName", Person_.firstName);
    tableAliasMap.put("name.middleName", Person_.middleName);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "name.middleName", Person_.middleName);
    tableAliasMap.put("active", Person_.active);
    tableAliasMap.put(ScimUser.SCHEMA_URI + "active", Person_.active);
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
      
      if (join == null) {
        AttributeReference attributePath = ace.getAttributePath();
        path = queryRoot.get((SingularAttribute<? super Person, ?>) tableAliasMap.get(ace.getAttributePath().getFullAttributeName()));
      } else {
        path = join.get(tableAliasMap.get(ace.getAttributePath().getFullAttributeName()));
      }
      
      if (path == null) {
        throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "Unable to map filter attribute " + ace.getAttributePath().getFullAttributeName());
      }
      
      switch(ace.getOperation()) {
        case EQ:
          return criteriaBuilder.equal(path, ace.getCompareValue());
        case CO:
          return criteriaBuilder.like(path, "*" + ace.getCompareValue() + "*");
        case EW:
          return criteriaBuilder.like(path, "*" + ace.getCompareValue());
        case GE:
          //return criteriaBuilder.greaterThanOrEqualTo(x, y);
          break;
        case GT:
          //return criteriaBuilder.greaterThan(x, y);
          break;
        case LE:
          //return criteriaBuilder.lessThanOrEqualTo(x, y);
          break;
        case LT:
          //return criteriaBuilder.lessThan(x, y);
          break;
        case NE:
          //return criteriaBuilder.notEqual(x, y);
          break;
        case SW:
          return criteriaBuilder.like(path, ace.getCompareValue() + "*");
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
