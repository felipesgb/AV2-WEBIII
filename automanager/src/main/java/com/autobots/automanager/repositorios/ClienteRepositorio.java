package com.autobots.automanager.repositorios;
import java.util.Optional;
import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import com.autobots.automanager.entidades.Cliente;
public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDocumentosId(Long id);
    Optional<Cliente> findByTelefonesId(Long id);
    Optional<Cliente> findByEnderecoId(Long id);
    @Query("select r.id as recursoId, c.id as clienteId from Cliente c join c.documentos r where r.id in :ids")
    List<VinculoCliente> donosDocumento(@Param("ids") Collection<Long> ids);

    @Query("select r.id as recursoId, c.id as clienteId from Cliente c join c.telefones r where r.id in :ids")
    List<VinculoCliente> donosTelefone(@Param("ids") Collection<Long> ids);

    @Query("select r.id as recursoId, c.id as clienteId from Cliente c join c.endereco r where r.id in :ids")
    List<VinculoCliente> donosEndereco(@Param("ids") Collection<Long> ids);

}
