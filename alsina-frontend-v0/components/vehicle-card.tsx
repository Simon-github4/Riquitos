"use client";

import Image from "next/image";
import Link from "next/link";
import { Calendar, Gauge, MapPin, ArrowRight } from "lucide-react";
import type { VehicleData } from "@/lib/vehicles";

interface VehicleCardProps {
  vehicle: VehicleData;
  index?: number;
}

export function VehicleCard({ vehicle, index = 0 }: VehicleCardProps) {
  const isRental = vehicle.type === "alquiler";

  return (
    <Link
      href={`/vehiculos/${vehicle.id}`}
      className="group relative bg-card border border-border rounded-2xl overflow-hidden hover:border-primary/50 transition-all duration-500 hover:shadow-xl hover:shadow-primary/5 block"
      style={{ animationDelay: `${index * 100}ms` }}
    >
      {/* Badge */}
      <div className="absolute top-4 left-4 z-10">
        <span className="bg-primary text-primary-foreground text-xs font-semibold px-3 py-1 rounded-md uppercase shadow-lg">
          {vehicle.type}
        </span>
      </div>

      {/* Image */}
      <div className="relative h-56 overflow-hidden bg-secondary">
        {/* Price Badge - on image bottom right */}
        <div className="absolute bottom-4 right-4 z-10">
          <span className="bg-background/90 backdrop-blur-sm text-foreground text-sm font-bold px-3 py-2 rounded-lg shadow-lg transition-all duration-300 group-hover:bg-primary group-hover:text-primary-foreground">
            $ {vehicle.price}
            {isRental && <span className="opacity-70 font-normal">/día</span>}
          </span>
        </div>
        <Image
          src={vehicle.image}
          alt={vehicle.name}
          fill
          className="object-cover transition-transform duration-700 group-hover:scale-110"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-background/80 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
        
        {/* Hover CTA */}
        <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all duration-300">
          <span className="bg-primary text-primary-foreground px-4 py-2 rounded-lg font-medium flex items-center gap-2 transform translate-y-4 group-hover:translate-y-0 transition-transform duration-300">
            Ver detalles
            <ArrowRight className="w-4 h-4" />
          </span>
        </div>
      </div>

      {/* Content */}
      <div className="p-5">
        <h3 className="text-lg font-bold text-foreground mb-3 group-hover:text-primary transition-colors">
          {vehicle.name}
        </h3>

        {/* Basic Details */}
        <div className="flex items-center gap-4 text-sm text-muted-foreground mb-3">
          <div className="flex items-center gap-1.5">
            <Calendar className="w-4 h-4 text-primary" />
            <span>{vehicle.year}</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Gauge className="w-4 h-4 text-primary" />
            <span>{vehicle.kilometers}</span>
          </div>
          <div className="flex items-center gap-1.5">
            <MapPin className="w-4 h-4 text-primary" />
            <span>{vehicle.location}</span>
          </div>
        </div>

        {isRental && (
          /* Tags */
          <div className="flex flex-wrap gap-2">
            {vehicle.insurance && (
              <span className="text-xs px-2 py-1 bg-secondary border border-border rounded-md text-muted-foreground">
                {vehicle.insurance}
              </span>
            )}
            {vehicle.franchise && (
              <span className="text-xs px-2 py-1 bg-secondary border border-border rounded-md text-muted-foreground">
                Franquicia {vehicle.franchise}
              </span>
            )}
          </div>
        )}
      </div>
    </Link>
  );
}
