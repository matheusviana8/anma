package com.anma.comercial.repository.produto;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.anma.comercial.model.Produto;
import com.anma.comercial.model.Produto_;
import com.anma.comercial.repository.filter.ProdutoFilter;

public class ProdutoRepositoryImpl implements ProdutoRepositoryQuery{
	
	@PersistenceContext
	private EntityManager manager;
	
	@Override
	public Page<Produto> filtrar(ProdutoFilter produtoFilter, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Produto> criteria = builder.createQuery(Produto.class);
		Root<Produto> root = criteria.from(Produto.class);
		
		Predicate[] predicates = criarRestricoes(produtoFilter, builder, root);
		criteria.where(predicates);
		
		TypedQuery<Produto> query = manager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query,pageable);
		
	    return new PageImpl<>(query.getResultList(), pageable, total(produtoFilter));
	}
	
	private Predicate[] criarRestricoes(ProdutoFilter produtoFilter, CriteriaBuilder builder,
			Root<Produto> root) {
		List<Predicate> predicates = new ArrayList<>();
			
		if (!StringUtils.isEmpty(produtoFilter.getId())) {
			predicates.add(builder.equal(root.get(Produto_.id), produtoFilter.getId()));
		}
		
		if (!StringUtils.isEmpty(produtoFilter.getDescricao())) {
			predicates.add(builder.like(
					builder.lower(root.get(Produto_.descricao)), "%" + produtoFilter.getDescricao().toLowerCase() + "%"));
		}
		
		return predicates.toArray(new Predicate[predicates.size()]);
	}	

	private void adicionarRestricoesDePaginacao(TypedQuery<?> query, Pageable pageable) {
		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistroDaPagina = paginaAtual * totalRegistrosPorPagina;
		
		query.setFirstResult(primeiroRegistroDaPagina);
		query.setMaxResults(totalRegistrosPorPagina);
	}
	
	private Long total(ProdutoFilter produtoFilter) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Produto> root = criteria.from(Produto.class);
		
		Predicate[] predicates = criarRestricoes(produtoFilter, builder, root);
		criteria.where(predicates);
		
		criteria.select(builder.count(root));
		return manager.createQuery(criteria).getSingleResult();
	}


}
