﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using AlbumStore.Common.Identity;
using AlbumStore.Domain.Entities;

namespace AlbumStore.Application.QueryProjections.Mappers
{
    public static class ProductMappers
    {
        public static IQueryable<ProductOverview> ToProductOverview(this IQueryable<Product> products, string currentUserId)
        {
            return products.Select(p => new ProductOverview
            {
                Id = p.Id,
                Name = p.Name,
                Price = p.Price,
                BandName = p.Band.Name,
                ArtistsName = string.Join(", ", p.Artists.Select(a => a.Name)),
                Image = p.BaseImageUrl,
                IsFavorited = p.UsersWhoLikeThisProduct.Any(u => u.Id == currentUserId)
            });
        }

    }
}
