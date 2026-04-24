"use client";

import Link from "next/link";
import Image from "next/image";
import { MapPin, Phone, Mail, Instagram, Facebook } from "lucide-react";
import { useEffect, useRef, useState } from "react";

const navigation = [
  { name: "Inicio", href: "/" },
  { name: "Vehículos en Venta", href: "/vehiculos?tipo=venta" },
  { name: "Vehículos en Alquiler", href: "/vehiculos?tipo=alquiler" },
  { name: "Todos los Vehículos", href: "/vehiculos" },
];

const branches = [
  { name: "Alsina — Casa Central", color: "bg-yellow-500" },
  { name: "Alsina 2 — Sucursal", color: "bg-yellow-500" },
  { name: "DelRio — Sucursal", color: "bg-yellow-500" },
];

const contactInfo = [
  { icon: Phone, text: "+54 11 5555-1234", href: "tel:+5411555512345" },
  { icon: Mail, text: "info@velocityvantage.com.ar", href: "mailto:info@velocityvantage.com.ar" },
];

const socials = [
  { icon: Instagram, href: "#", label: "Instagram" },
  { icon: Facebook, href: "#", label: "Facebook" },
];

export function Footer() {
  const [isVisible, setIsVisible] = useState(false);
  const footerRef = useRef<HTMLElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
        }
      },
      { threshold: 0.1 }
    );

    if (footerRef.current) {
      observer.observe(footerRef.current);
    }

    return () => observer.disconnect();
  }, []);

  return (
    <footer ref={footerRef} className="bg-card border-t border-border">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div
          className={`grid md:grid-cols-4 gap-12 transition-all duration-700 ${
            isVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
          }`}
        >
          {/* Brand */}
          <div>
            <Link href="/" className="flex items-center gap-2 mb-4 group">
              <Image
                src="/Alsina.png"
                alt="Alsina"
                width={40}
                height={40}
                className="object-contain rounded-lg"
              />
            </Link>
            <p className="text-muted-foreground text-sm mb-6">
              Venta y alquiler de vehículos seleccionados desde 2009.
            </p>
            <div className="flex gap-3">
              {socials.map((social) => (
                <a
                  key={social.label}
                  href={social.href}
                  className="w-10 h-10 bg-secondary rounded-lg flex items-center justify-center text-muted-foreground hover:text-foreground hover:bg-primary/20 transition-colors"
                  aria-label={social.label}
                >
                  <social.icon className="w-5 h-5" />
                </a>
              ))}
            </div>
          </div>

          {/* Navigation */}
          <div>
            <h3 className="text-foreground font-semibold mb-4">Navegación</h3>
            <ul className="space-y-3">
              {navigation.map((item) => (
                <li key={item.name}>
                  <Link
                    href={item.href}
                    className="text-muted-foreground hover:text-primary transition-colors text-sm"
                  >
                    {item.name}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Branches */}
          <div>
            <h3 className="text-foreground font-semibold mb-4">Sucursales</h3>
            <ul className="space-y-3">
              {branches.map((branch) => (
                <li key={branch.name} className="flex items-center gap-2 group cursor-pointer">
                  <MapPin className="w-4 h-4 text-yellow-500 transition-transform group-hover:scale-110" />
                  <span className="text-muted-foreground text-sm group-hover:text-foreground transition-colors">
                    {branch.name}
                  </span>
                </li>
              ))}
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h3 className="text-foreground font-semibold mb-4">Contacto</h3>
            <ul className="space-y-3">
              {contactInfo.map((item) => (
                <li key={item.text}>
                  <a
                    href={item.href}
                    className="flex items-center gap-2 text-muted-foreground hover:text-primary transition-colors"
                  >
                    <item.icon className="w-4 h-4 text-primary" />
                    <span className="text-sm">{item.text}</span>
                  </a>
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Large Logo Text */}
        <div
          className={`mt-16 text-center transition-all duration-1000 delay-300 ${
            isVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
          }`}
        >
          <div className="text-6xl sm:text-8xl lg:text-9xl font-black text-muted/20 tracking-tight select-none">
            ALSINA
          </div>
        </div>

        {/* Copyright */}
        <div
          className={`mt-8 pt-8 border-t border-border text-center transition-all duration-700 delay-500 ${
            isVisible ? "opacity-100" : "opacity-0"
          }`}
        >
          <p className="text-muted-foreground text-sm">
            © {new Date().getFullYear()} Alsina Automóviles Multimarca. Todos los derechos reservados.
          </p>
        </div>
      </div>
    </footer>
  );
}
