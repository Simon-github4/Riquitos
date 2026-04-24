"use client";

import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { VehicleCard } from "./vehicle-card";
import { allVehicles } from "@/lib/vehicles";
import { useEffect, useRef, useState } from "react";

export function VehiclesSection() {
  const [saleVisible, setSaleVisible] = useState(false);
  const [rentalVisible, setRentalVisible] = useState(false);
  const saleRef = useRef<HTMLElement>(null);
  const rentalRef = useRef<HTMLElement>(null);

  const saleVehicles = allVehicles.filter((v) => v.type === "venta").slice(0, 3);
  const rentalVehicles = allVehicles.filter((v) => v.type === "alquiler").slice(0, 3);

  useEffect(() => {
    const observerCallback = (entries: IntersectionObserverEntry[]) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          if (entry.target === saleRef.current) setSaleVisible(true);
          if (entry.target === rentalRef.current) setRentalVisible(true);
        }
      });
    };

    const observer = new IntersectionObserver(observerCallback, { threshold: 0.1 });

    if (saleRef.current) observer.observe(saleRef.current);
    if (rentalRef.current) observer.observe(rentalRef.current);

    return () => observer.disconnect();
  }, []);

  return (
    <>
      {/* Sale Section */}
      <section ref={saleRef} id="venta" className="py-24 bg-secondary">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div
            className={`flex items-center justify-between mb-12 transition-all duration-700 ${
              saleVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
            }`}
          >
            <div>
              <span className="text-primary text-sm font-semibold tracking-widest uppercase">
                Venta
              </span>
              <h2 className="text-3xl sm:text-4xl font-black text-foreground mt-2">
                UNIDADES DESTACADAS
              </h2>
            </div>
            <Link
              href="/vehiculos?tipo=venta"
              className="hidden sm:flex items-center gap-2 text-muted-foreground hover:text-primary transition-colors group"
            >
              Ver todos
              <ArrowRight className="w-4 h-4 transition-transform group-hover:translate-x-1" />
            </Link>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {saleVehicles.map((vehicle, index) => (
              <div
                key={vehicle.id}
                className={`transition-all duration-700 ${
                  saleVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-12"
                }`}
                style={{ transitionDelay: `${index * 150 + 200}ms` }}
              >
                <VehicleCard vehicle={vehicle} index={index} />
              </div>
            ))}
          </div>

          <Link
            href="/vehiculos?tipo=venta"
            className="flex sm:hidden items-center justify-center gap-2 text-muted-foreground hover:text-primary transition-colors mt-8"
          >
            Ver todos
            <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      </section>

      {/* Rental Section */}
      <section ref={rentalRef} id="alquiler" className="py-24 bg-background">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div
            className={`flex items-center justify-between mb-12 transition-all duration-700 ${
              rentalVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
            }`}
          >
            <div>
              <span className="text-primary text-sm font-semibold tracking-widest uppercase">
                Alquiler
              </span>
              <h2 className="text-3xl sm:text-4xl font-black text-foreground mt-2">
                FLOTA DE ALQUILER
              </h2>
            </div>
            <Link
              href="/vehiculos?tipo=alquiler"
              className="hidden sm:flex items-center gap-2 text-muted-foreground hover:text-primary transition-colors group"
            >
              Ver todos
              <ArrowRight className="w-4 h-4 transition-transform group-hover:translate-x-1" />
            </Link>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {rentalVehicles.map((vehicle, index) => (
              <div
                key={vehicle.id}
                className={`transition-all duration-700 ${
                  rentalVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-12"
                }`}
                style={{ transitionDelay: `${index * 150 + 200}ms` }}
              >
                <VehicleCard vehicle={vehicle} index={index} />
              </div>
            ))}
          </div>

          <Link
            href="/vehiculos?tipo=alquiler"
            className="flex sm:hidden items-center justify-center gap-2 text-muted-foreground hover:text-primary transition-colors mt-8"
          >
            Ver todos
            <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      </section>
    </>
  );
}
