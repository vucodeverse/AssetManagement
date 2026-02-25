    package edu.fpt.groupfive.dao.warehouse;

    import edu.fpt.groupfive.model.warehouse.Warehouse;

    import java.util.List;
    import java.util.Optional;

    public interface WarehouseDAO {

        Optional<Warehouse> findById(Integer id);

        List<Warehouse> findAll();

        boolean existsById(Integer id);

        void deleteById(Integer id);

        Warehouse create(Warehouse newWarehouse);

        Warehouse update(Warehouse warehouse);
    }
