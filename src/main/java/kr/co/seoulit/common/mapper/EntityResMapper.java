package kr.co.seoulit.common.mapper;

public interface EntityResMapper <E, D> {

    D toDto(E entity);
   // List<D> toDto(List<E> entities);

}
