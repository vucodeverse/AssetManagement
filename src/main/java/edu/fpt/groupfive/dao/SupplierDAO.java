package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.Supplier;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface SupplierDAO{

    Optional<Supplier> getAllSupplier();
}
