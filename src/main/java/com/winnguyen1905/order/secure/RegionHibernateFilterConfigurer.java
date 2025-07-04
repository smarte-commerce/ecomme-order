package com.winnguyen1905.order.secure;

import java.io.IOException;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.winnguyen1905.order.secure.AccountRequestArgumentResolver.AccountRequestArgument;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RegionHibernateFilterConfigurer extends OncePerRequestFilter {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
      String region = jwt.getClaimAsString(AccountRequestArgument.REGION.value);
      Session session = entityManager.unwrap(Session.class);
      Filter filter = session.getEnabledFilter("regionFilter");
      if (region != null) {
        if (filter == null) {
          session.enableFilter("regionFilter").setParameter("region", region);
        } else {
          filter.setParameter("region", region);
        }
      } else {
        if (filter != null) {
          session.disableFilter("regionFilter");
        }
      }
    }

    filterChain.doFilter(request, response);
  }
}
