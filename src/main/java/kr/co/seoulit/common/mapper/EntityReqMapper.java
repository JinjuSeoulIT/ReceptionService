package kr.co.seoulit.common.mapper;

public interface EntityReqMapper <E, D> {

    E toEntity(D dto);
   // List<E> toEntity(List<D> dtos);



}
