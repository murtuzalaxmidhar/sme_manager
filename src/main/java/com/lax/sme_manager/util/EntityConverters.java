package com.lax.sme_manager.util;

import com.lax.sme_manager.domain.Vendor;
import com.lax.sme_manager.repository.model.VendorEntity;

import java.time.LocalDateTime;

public class EntityConverters {

    private EntityConverters() {}

    /* UI → DB */
    public static VendorEntity toEntity(Vendor vendor) {
        LocalDateTime now = LocalDateTime.now();

        VendorEntity e = new VendorEntity();
        e.setId(vendor.getId());
        e.setName(vendor.getName().trim());

        // defaults for fields not captured in purchase screen
        e.setContactPerson("");
        e.setAddress("");
        e.setPhone("");
        e.setEmail("");
        e.setNotes("From Purchase Screen");

        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        return e;
    }

    /* DB → UI */
    public static Vendor toDomain(VendorEntity entity) {
        return new Vendor(entity.getId(), entity.getName());
    }

    /* helper for ComboBox typed entry */
    public static Vendor newVendor(String name) {
        return new Vendor(-1, name.trim());
    }
}

